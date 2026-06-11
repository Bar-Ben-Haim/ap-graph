# ap-graph

A message-based **computational graph** system in Java. Users define computational nodes
(**Agents**) and data streams (**Topics**) in a configuration file, upload it through a
web dashboard served by a **custom-built HTTP server**, and watch the graph compute and
update live as messages are published.

## Overview

`ap-graph` implements a **publish/subscribe** architecture:

- **Topics** are named message channels.
- **Agents** subscribe to topics, perform a computation when a message arrives, and
  publish results to other topics.
- **Configurable graphs** are described in plain-text `.conf` files (one agent per
  three lines). Topics are created implicitly from the names referenced.
- A **web interface** lets you upload a configuration, visualize the resulting directed
  graph, publish values to topics, and watch each topic's current value.

The graph must be a **DAG** — cyclic configurations are rejected.

## Features

- **Custom HTTP server** — multi-threaded (`MyHTTPServer`), handles `GET`/`POST`/`DELETE`,
  multipart uploads, and longest-prefix URI routing to servlets.
- **Layered architecture** — `controller (servlets) → service → repository → model`,
  for clear separation of concerns (SOLID).
- **Dynamic graph loading** — upload a `.conf` to instantiate and run a graph on the fly;
  **reset** to rebuild it (clearing state) and **delete** to tear it down.
- **Agent library** — arithmetic (`PlusAgent`, `MulAgent`, `IncAgent`) and statistics
  (`MovingAverageAgent`, `MaxTrackerAgent`, `CounterAgent`) agents over reusable
  `BinOpAgent` / `UnaryOpAgent` bases.
- **Concurrency** — each agent runs as an `Active Object` (`ParallelAgent` decorator) with
  its own worker thread and bounded queue; topics are managed by a thread-safe singleton.
- **Live visualization** — a `vis-network` graph (topics = boxes, agents = circles) plus a
  topic-values table, both refreshed as messages flow.
- **Typed error handling** — every failure maps to a `ConfigError` (single source of truth)
  rendered by a severity-styled error page.
- **Input hardening** — uploaded file names are sanitized and script/markup content is
  rejected; uploaded configs are stored in an app-owned directory (never the system temp dir).

## Architecture

```
Browser ──HTTP──▶ MyHTTPServer ──▶ Servlet (controller layer)
                                     │
   POST /upload ─ ConfLoader ────────┤
   POST /reset  ─ ConfigReset ───────┼──▶ GraphService (service layer)
   DELETE /delete ─ ConfigDelete ────┤        ├──▶ GraphRepository      (in-memory active Graph)
                                     │        ├──▶ FileRepository        (persisted .conf files)
                                     │        └──▶ GenericConfig ──▶ Agents + Topics (model)
   GET /publish ─ TopicDisplayer ────┼──▶ TopicManagerSingleton.publish(...)
   GET /graph   ─ GraphDisplayer ────┼──▶ HtmlGraphWriter (view: Graph → HTML)
   GET /app/**  ─ HtmlLoader ────────┘──▶ static files in html_files/
```

- **Model** (`graph`, `configs`): `Message`, `Topic`, `Agent`, `TopicManagerSingleton`,
  `ParallelAgent`, `Node`, `Graph`, agent implementations.
- **Repository** (`repository`): `GraphRepository`/`LocalGraphRepository` (the active graph)
  and `FileRepository`/`LocalFileRepository` (uploaded configs by name).
- **Service** (`service`): `GraphService` owns the deploy/reset/delete lifecycle and is the
  single funnel where uploads are validated and sanitized.
- **Controller** (`server`, `servlets`): the HTTP server, request parsing, and servlets.
- **View** (`views`): `HtmlGraphWriter` injects graph data into the static templates.

## HTTP API

| Method   | Path                             | Servlet          | Purpose                                                                                   |
|----------|----------------------------------|------------------|-------------------------------------------------------------------------------------------|
| `GET`    | `/app/<file>`                    | `HtmlLoader`     | Serve a static file from `html_files/` (e.g. `index.html`).                               |
| `POST`   | `/upload`                        | `ConfLoader`     | Upload a `.conf` (multipart); deploy it; return the graph HTML (or a typed error page).   |
| `GET`    | `/graph`                         | `GraphDisplayer` | Render the current graph, or a "no graph deployed" page.                                  |
| `GET`    | `/publish?topic=<t>&message=<m>` | `TopicDisplayer` | Publish a message to a topic; return the topic-values table.                              |
| `POST`   | `/reset`                         | `ConfigReset`    | Rebuild the active graph from its saved config (clears all topic values and agent state). |
| `DELETE` | `/delete`                        | `ConfigDelete`   | Remove the configuration and graph entirely (back to "nothing deployed").                 |

## Web Interface

Open <http://localhost:8080/app/index.html>. The page is a responsive 3-pane shell
(`iframe`s) that stacks vertically on narrow windows:

- **Left — Controls** (`form.html`): deploy a `.conf` (button **deploy** → `POST /upload`),
  publish a message to a topic (→ `GET /publish`), and **Graph Control** buttons
  **Reset** (`POST /reset`) and **Delete** (`DELETE /delete`).
- **Center — Graph** (`graph.html`): the live `vis-network` visualization. Topics are blue
  boxes (with their current value), agents are green circles; arrows follow the data flow.
- **Right — Topic Values** (`/publish`): a table of each topic's latest value, refreshed on
  publish/reset/delete.
- **Errors** (`error.html`): a severity-styled page (info / warning / error) used for
  cycles, malformed configs, unknown agents, etc.

## Writing a Configuration File (`.conf`)

A graph is parsed by `GenericConfig` in **groups of exactly three lines**, one group per
agent (total line count must be a multiple of 3, no blank lines or comments):

```text
<fully-qualified agent class name>
<comma-separated input topic names>      # topics the agent subscribes to (subs)
<comma-separated output topic names>     # topics the agent publishes to   (pubs)
```

- **Topics are created implicitly** — any name referenced is created on first use.
- **The agent class must expose a `(String[] subs, String[] pubs)` constructor.** The base
  classes `BinOpAgent` / `UnaryOpAgent` do **not**, so they cannot be referenced directly.
- **Each agent class should appear at most once** — graph nodes are keyed by agent name, so
  duplicates collapse into one node and can create a misleading graph or a false cycle.
- **The graph must be acyclic (a DAG)** — cyclic configs are rejected (`CYCLES_DETECTED`).

### Example

The bundled [`config_files/simple.conf`](config_files/simple.conf):

```text
project_biu.configs.agents.PlusAgent
A,B
C
project_biu.configs.agents.MulAgent
A,C
D
project_biu.configs.agents.IncAgent
D
E
```

`C = A + B`, then `D = A * C`, then `E = D + 1`. Publish to topics `A` and `B` from the
**Publish To Topic** form to drive the computation.

## Agents

Each agent subscribes to input topics (`subs`), reacts to incoming `Message`s, and publishes
to its output topics (`pubs`). Non-numeric messages are ignored, and agents that combine two
inputs wait until **both** have been received before producing output.

> **Order-independent agents only.** The graph draws edges between topics and agents but does
> not show which input/output *slot* an edge maps to, so only single-input or commutative
> (`PlusAgent`, `MulAgent`) agents are provided.

| Agent (class)        | subs      | pubs      | Description                                                |
|----------------------|-----------|-----------|------------------------------------------------------------|
| `PlusAgent`          | `x, y`    | `sum`     | Publishes `x + y` whenever either input changes.           |
| `MulAgent`           | `x, y`    | `product` | Publishes `x * y` whenever either input changes.           |
| `IncAgent`           | `x`       | `x + 1`   | Publishes the input incremented by one.                    |
| `MovingAverageAgent` | `value`   | `average` | Running average of the values seen so far.                 |
| `MaxTrackerAgent`    | `value`   | `max`     | Running maximum; publishes only when a new record arrives. |
| `CounterAgent`       | `trigger` | `count`   | Counts every message received and publishes the count.     |

> **Base classes (not usable directly in a `.conf`):** `BinOpAgent` applies any
> `BinaryOperator<Double>` to two inputs (parent of `PlusAgent`/`MulAgent`); `UnaryOpAgent`
> applies any `UnaryOperator<Double>` to one input (parent of `IncAgent`).

### Adding a new agent

1. Create a class in `project_biu.configs.agents` that `implements Agent` (or extends a base).
2. Provide a `public MyAgent(String[] subs, String[] pubs)` constructor that subscribes to its
   inputs and registers as a publisher on its outputs.
3. Reference it by fully qualified class name in a `.conf`.

## Security & input handling

Uploaded data is untrusted, so it is sanitized in one place — `GraphService.deploy(...)`,
using `project_biu.utils.FileSanitizer`:

- **File names** are reduced to a safe form (directory parts stripped, unsafe characters
  replaced, leading dots removed) before being used as a storage key — defense-in-depth on
  top of the repository's path-traversal guard.
- **Content** containing HTML/script-like text (`<script>`, any `<…>` tag, `javascript:`,
  inline `on…=` handlers) is rejected with `ConfigError.UNSAFE_CONTENT`.
- **Storage location**: configs are written to an application-owned directory
  (`uploaded_configs/`, configurable), never the shared system temp directory.

## Requirements

- **Java**: JDK 25 (`maven.compiler.source/target = 25`).
- **Maven** 3.9+.
- **Dependencies** (see `pom.xml`): Jackson Databind 2.21.3, Commons IO 2.16.1,
  SLF4J 2.0.18 + Log4j 2.26.0, JUnit Jupiter 5.10.0 (test).

## Setup & Run

### Build

```powershell
mvn clean package
```

This produces a shaded (fat) jar at `target/ap-graph-1.0-SNAPSHOT.jar`
(main class `project_biu.Main`).

### Run

> Run from the **project root**, because the server loads `html_files/` and writes
> `uploaded_configs/` relative to the working directory.

```powershell
java -jar target/ap-graph-1.0-SNAPSHOT.jar
```

The server starts on port **8080**. It stays up until you press **Enter** in the terminal
(the process blocks on `System.in.read()`), then shuts down.

Open <http://localhost:8080/app/index.html>.

### Docker

The image bundles `html_files/`. Because the app blocks on stdin, run it interactively:

```powershell
docker build -t ap-graph .
docker run -it -p 8080:8080 ap-graph
```

## Demo

> TODO: add demo video

## Project Structure

```text
ap-graph/
├── src/main/java/project_biu/
│   ├── Main.java                 # Entry point: wires repositories, service, servlets
│   ├── configs/                  # Config, GenericConfig, Graph, Node, ConfigError, ConfigException
│   │   └── agents/               # BinOpAgent, UnaryOpAgent, Plus/Mul/Inc/MovingAverage/MaxTracker/Counter
│   ├── graph/                    # Message, Topic, Agent, ParallelAgent, TopicManagerSingleton
│   ├── repository/               # GraphRepository + FileRepository (and Local* impls)
│   ├── service/                  # GraphService (deploy/reset/delete lifecycle)
│   ├── server/                   # HTTPServer, MyHTTPServer, RequestParser
│   │   └── response/              # ResponseUtils, StatusCode, MediaType
│   ├── servlets/                 # Servlet + ConfLoader/ConfigReset/ConfigDelete/TopicDisplayer/GraphDisplayer/HtmlLoader
│   ├── utils/                    # InputSanitizer
│   └── views/                    # HtmlGraphWriter (Graph → HTML)
├── src/test/java/test/           # Unit tests
├── config_files/                 # Example .conf files (simple.conf)
├── html_files/                   # Static web assets (index/form/graph/temp/error .html)
├── uploaded_configs/             # Runtime store for uploaded configs (gitignored)
├── docs/                         # Requirements checklist, review notes, UI/UX proposal
├── Dockerfile
├── pom.xml
└── README.md
```

## Tests

```powershell
mvn test
```

JUnit 5 tests cover the HTTP server, request parsing, and core graph logic.

## License

TODO: add license information.
