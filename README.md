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

The graph must be a **DAG** — A graph with cycles is rejected.

## Live now

> - The project is now running on cloud **Railway** and deployed
    automatically commit to branch `main` (since having Dockerfile):
    <https://ap-graph-production.up.railway.app/app/index.html>
> - The project is running in serverless mode to cut expenses, so it may take a while to
    respond to the first request.

## Requirements

- **Java**: JDK 25 (`maven.compiler.source/target = 25`).
- **Maven** 3.9+.
- **Dependencies** (see `pom.xml`): Jackson Databind 2.21.3, Commons IO 2.16.1,
  SLF4J 2.0.18 + Log4j 2.26.0, JUnit Jupiter 5.10.0 (test).

## Setup & Run

### Build

```powershell
mvn clean package -DskipTests
```

This produces a shaded (fat) jar at `target/ap-graph-1.0-SNAPSHOT.jar`
(main class `project_biu.Main`).

### Run (locally)

> Run from the **project root**, because the server loads `html_files/` and writes
> `uploaded_configs/` relative to the working directory.

```powershell
java -jar target/ap-graph-1.0-SNAPSHOT.jar
```

The server starts on port **8080**. It stays up until you press
**any char key (but -1)** in the terminal, then shuts down.
When stdin is closed (For exmaple when running in a container) it instead blocks on
a `CountDownLatch` and keeps serving until shutdown, when the hook handles the closure
of the resources cleanly.

Open <http://localhost:8080/app/index.html>.

### Docker

The image bundles `html_files/`. The container keeps running without a TTY (it falls back to
the `CountDownLatch` keep-alive), so stop it with `docker stop`:

```powershell
docker build -t ap-graph .
docker run -p 8080:8080 ap-graph
```

## Javadocs & API

This project is documented with [Javadocs](http://localhost:63342/ap-graph/javadoc/index.html),
you can use the link to view and learn about the API of this project.<p>
Try to create your own server and servlets with the libraries and interfaces
of this project.

## Demo
[ap-graph-final.mp4](docs/ap-graph-final.mp4)

## Project Structure

```text
ap-graph/
├── src/main/java/project_biu/
│   ├── Main.java                 # Entry point: wires repositories, service, servlets
│   ├── configs/                  # Config, GenericConfig, Graph, Node, ConfigError, ConfigException
│   │   └── agents/               # All the Agents impls
│   ├── graph/                    # Message, Topic, Agent, MathematicalDescribable, ParallelAgent, TopicManagerSingleton
│   ├── repository/               # GraphRepository + FilesRepository (+ Local* impls)
│   ├── service/                  # GraphService
│   ├── server/                   # HTTPServer, MyHTTPServer, RequestParser, MultipartParser
│   │   └── response/             # ResponseUtils, StatusCode, MediaType
│   ├── servlets/                 # Servlets
│   ├── utils/                    # FileUtils, NumberFormatter, ReplaceUntrustedChars
│   └── views/                    # HtmlGraphWriter (Graph → HTML), HtmlErrorWriter (error page)
├── src/main/resources/           # log4j2.xml (logging config)
├── src/test/java/test/           # Unit tests
├── config_files/                 # Example .conf files (simple1/simple2/not-simple/empty/conf-with-cycles/invalid)
├── html_files/                   # Static web assets
├── uploaded_configs/             # Runtime store for uploaded configs
├── Dockerfile
├── pom.xml
└── README.md
```

## Features

- **Custom HTTP server** — multithreaded (`MyHTTPServer`), handles `GET`/`POST`/`DELETE`,
  multipart uploads, and longest-prefix URI routing to servlets.
- **Layered architecture** — `controller (servlets) → service → repository → model`,
  for clear separation of concerns (SOLID).
- **Dynamic graph loading** — upload a `.conf` to instantiate and run a graph on the fly;
  **reset** to rebuild it (clearing state) and **delete** to tear it down.
- **Agent library** — arithmetic (`PlusAgent`, `MinusAgent`, `MulAgent`, `IncAgent`) and
  statistics (`MovingAverageAgent`, `MaxTrackerAgent`, `CounterAgent`) agents over reusable
  abstract `BinOpAgent` / `UnaryOpAgent` bases.
- **Symbolic formula derivation** — every agent is `MathematicalDescribable`, so the graph
  walks the DAG from each output topic back to its inputs and prints a simplified algebraic
  formula (e.g. `E = (A + B) * (A - B)`), shown in the visualization legend.
- **Concurrency** — each agent runs as an `Active Object` (`ParallelAgent` decorator) with
  its own worker thread and bounded queue; topics are managed by a thread-safe singleton.
- **Live visualization** — a `vis-network` graph (topics = boxes, agents = circles, agents
  also labeled with their formula) plus a topic-values table, both refreshed as messages
  flow, and a **Graph simplified Formulas** panel in the legend.
- **Typed error handling** — every failure maps to a `ConfigError` (single source of truth)
  rendered by a severity-styled error page.
- **Input hardening** — uploaded file names are sanitized, and script/markup content is
  rejected; uploaded configs are stored in an app-owned directory (never the system temp dir).
- **Logging** writing to the console as well as to the file with rolling policy. Used `slf4j` interfaces
  with `log4j2` implementation (see `resources/log4j2.xml`).

## Architecture

```
Browser ──HTTP──▶ MyHTTPServer ──▶ Servlet (controller layer)
                                     │
   POST /upload ─ ConfLoader ────────┤
   POST /reset  ─ ConfigReset ───────┼──▶ GraphService (service layer)
   DELETE /delete ─ ConfigDelete ────┤        ├──▶ GraphRepository      (in-memory active Graph)
                                     │        ├──▶ FilesRepository       (persisted .conf files)
                                     │        └──▶ GenericConfig ──▶ Agents + Topics (model)
   GET /publish ─ TopicDisplayer ────┼──▶ TopicManagerSingleton.publish(...)
   GET /graph   ─ GraphDisplayer ────┼──▶ HtmlGraphWriter (view: Graph → HTML)
   GET /app/**  ─ HtmlLoader ────────┘──▶ static files in html_files/
```

- **Model** (`graph`, `configs`): `Message`, `Topic`, `Agent`, `MathematicalDescribable`,
  `TopicManagerSingleton`, `ParallelAgent`, `Node`, `Graph`, agent implementations.
- **Repository** (`repository`): `GraphRepository`/`LocalGraphRepository` (the active graph)
  and `FilesRepository`/`LocalFilesRepository` (uploaded configs by name).
- **Service** (`service`): `GraphService` owns the deploy/reset/delete lifecycle and is the
  single funnel where uploads are validated and sanitized.
- **Controller** (`server`, `servlets`): the HTTP server, request parsing (`RequestParser`),
  multipart body parsing (`MultipartParser`), and servlets.
- **View** (`views`): `HtmlGraphWriter` injects graph data and formulas into the static
  template; `HtmlErrorWriter` renders the severity-styled error page.

## HTTP API

| Method   | Path                             | Servlet          | Purpose                                                                                   |
|----------|----------------------------------|------------------|-------------------------------------------------------------------------------------------|
| `GET`    | `/app/<file>`                    | `HtmlLoader`     | Serve a static file from `html_files/` (e.g. `index.html`).                               |
| `POST`   | `/upload`                        | `ConfLoader`     | Upload a `.conf` (multipart); deploy it; return the graph HTML (or a typed error page).   |
| `GET`    | `/graph`                         | `GraphDisplayer` | Render the current graph, or a "no graph deployed" page.                                  |
| `GET`    | `/publish?topic=<t>&message=<m>` | `TopicDisplayer` | Publish a message to a topic; return the topic-values table (`404` if the topic doesn't exist). |
| `POST`   | `/reset`                         | `ConfigReset`    | Rebuild the active graph from its saved config (clears all topic values and agent state). |
| `DELETE` | `/delete`                        | `ConfigDelete`   | Remove the configuration and graph entirely (back to "nothing deployed").                 |

## Web Interface

Open <http://localhost:8080/app/index.html>. The page is a responsive 3-pane shell
(`iframe`s) that stacks vertically on narrow windows:

- **Left — Controls** (`form.html`): deploy a `.conf` (button **deploy** → `POST /upload`),
  publish a message to a topic (→ `GET /publish`; publishing to an unknown topic returns
  `404` and the page shows a toast), and **Graph Control** buttons
  **Reset** (`POST /reset`) and **Delete** (`DELETE /delete`).
- **Center — Graph** (`graph.html`): the live `vis-network` visualization. Topics are
  boxes (with their current value), agents are circles labelled with their formula; arrows
  follow the data flow. The legend includes a **Graph simplified Formulas** panel listing
  the derived algebraic formula for each output topic.
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

The bundled [`config_files/simple2.conf`](config_files/simple2.conf):

```text
project_biu.configs.agents.PlusAgent
A,B
C
project_biu.configs.agents.MinusAgent
A,B
D
project_biu.configs.agents.MulAgent
C,D
E
```

`C = A + B`, and `D = A - B`, then `E = C * D`. Publish to topics `A` and `B` from the
**Publish To Topic** form to drive the computation. The legend's formula panel collapses the
chain into `E = (A + B) * (A - B)`. The bundled
[`config_files/simple1.conf`](config_files/simple1.conf) shows multi-producer derivation
(two agents publish to the same topic), and
[`config_files/not-simple.conf`](config_files/not-simple.conf) chains all six agent types.
The error-demo configs trigger typed errors:
[`conf-with-cycles.conf`](config_files/conf-with-cycles.conf) (`CYCLES_DETECTED`),
[`invalid.conf`](config_files/invalid.conf) (`UNKNOWN_AGENT`), and
[`empty.conf`](config_files/empty.conf) (`EMPTY_CONFIG`).

## Agents

Each agent subscribes to input topics (`subs`), reacts to incoming `Message`s, and publishes
to its output topics (`pubs`). Non-numeric messages are ignored, and agents that combine two
inputs wait until **both** have been received before producing output.

| Agent (class)        | subs      | pubs      | Formula          | Description                                                |
|----------------------|-----------|-----------|------------------|------------------------------------------------------------|
| `PlusAgent`          | `x, y`    | `sum`     | `(x + y)`        | Publishes `x + y` whenever either input changes.           |
| `MinusAgent`         | `x, y`    | `diff`    | `(x - y)`        | Publishes `x - y` whenever either input changes.           |
| `MulAgent`           | `x, y`    | `product` | `(x * y)`        | Publishes `x * y` whenever either input changes.           |
| `IncAgent`           | `x`       | `x + 1`   | `(x + 1)`        | Publishes the input incremented by one.                    |
| `MovingAverageAgent` | `value`   | `average` | `AVG(value)`     | Running average of the values seen so far.                 |
| `MaxTrackerAgent`    | `value`   | `max`     | `MAX(value)`     | Running maximum; publishes only when a new record arrives. |
| `CounterAgent`       | `trigger` | `count`   | `COUNT(trigger)` | Counts every message received and publishes the count.     |

Each agent implements `MathematicalDescribable` (`getMathPattern` / `getMathRepresentation`),
which the graph composes into the simplified per-topic formulas shown in the legend.

> **Base classes (abstract — not usable directly in a `.conf`):** `BinOpAgent` applies any
> `BinaryOperator<Double>` to two inputs (parent of `PlusAgent`/`MinusAgent`/`MulAgent`);
> `UnaryOpAgent` applies any `UnaryOperator<Double>` to one input (parent of `IncAgent`).

### Adding a new agent

1. Create a class in `project_biu.configs.agents` that `implements Agent` (or extends a base)
   and `implements MathDescribable` (if it is a Mathematical Agent).
2. Provide a `public MyAgent(String[] subs, String[] pubs)` constructor that subscribes to its
   inputs and registers as a publisher on its outputs.
3. Override `getMathPattern(String... inputs)` so the agent renders its formula in the graph
   (extending `BinOpAgent`/`UnaryOpAgent` supplies `getMathRepresentation` for free; a raw
   `implements Agent, MathematicalDescribable` provides both).
4. Reference it by fully qualified class name in a `.conf`.

## Security & input handling

Uploaded data is untrusted, so it is sanitized in one place — `GraphService.deploy(...)`,
using `project_biu.utils.FileUtils`:

- **File names** are reduced to a safe form (directory parts stripped, unsafe characters
  replaced, leading dots removed) before being used as a storage key — defense-in-depth on
  top of the repository's path-traversal guard.
- **Content** containing HTML/script-like text (`<script>`, any `<…>` tag, `javascript:`,
  inline `on…=` handlers) is rejected with `ConfigError.UNSAFE_CONTENT`.
- **Storage location**: configs are written to an application-owned directory
  (`uploaded_configs/`, configurable), never the shared system temp directory.

## Tests

```powershell
mvn test
```

JUnit 5 tests cover the HTTP server, request parsing, and core graph logic.