# ap-graph

A message-based computational graph system in Java. This project allows users to define computational nodes (Agents) and data streams (Topics) via configuration files, visualize the graph, and interact with it through a custom-built HTTP server and web interface.

## Overview

The `ap-graph` system implements a publish-subscribe architecture where:
- **Topics**: Act as message channels.
- **Agents**: Functional units that subscribe to topics, perform operations (e.g., addition, multiplication), and publish results back to other topics.
- **Configurable Graphs**: Graphs are defined in `.conf` files, specifying nodes (Agents and Topics) and their connections.
- **Web Interface**: A custom HTTP server provides a dashboard to upload configurations, view the graph visualization, and monitor topic values.

## Features

- **Custom HTTP Server**: Multi-threaded server handling GET/POST requests and multipart file uploads.
- **Dynamic Graph Loading**: Upload `.conf` files to instantiate and run computational graphs on the fly.
- **Visualization**: Generates HTML representations of the active computational graph.
- **Agents Library**: Includes `PlusAgent`, `MulAgent`, `IncAgent`, `BinOpAgent`, and `UnaryOpAgent`.
- **Topic Management**: Centralized `TopicManager` for handling subscriptions and message delivery.

## Requirements

- **Java**: JDK 25 (as specified in `pom.xml`).
- **Maven**: For dependency management and building.
- **Dependencies**:
  - Jackson Databind (2.15.2)
  - Commons IO (2.16.1)
  - JUnit Jupiter (5.10.0) for testing.

## Setup & Run

### 1. Build the project
```powershell
mvn clean install
```

### 2. Run the application
The main entry point is `project_biu.Main`. You can run it via Maven:
```powershell
mvn exec:java -Dexec.mainClass="project_biu.Main"
```
Or by running the compiled JAR (if packaged).

The server starts by default on port `8080`.

### 3. Access the Web Interface
Open your browser and navigate to:
[http://localhost:8080/app/index.html](http://localhost:8080/app/index.html)

## Scripts

- `mvn clean compile`: Compiles the source code.
- `mvn test`: Runs unit tests.
- `mvn exec:java -Dexec.mainClass="project_biu.Main"`: Starts the application.

## Project Structure

```text
ap-graph/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── project_biu/
│   │   │   │   ├── Main.java           # Entry point
│   │   │   │   ├── configs/            # Graph configuration and nodes
│   │   │   │   ├── graph/              # Core Topic/Agent logic
│   │   │   │   ├── server/             # Custom HTTP server implementation
│   │   │   │   ├── servlets/           # Web request handlers
│   │   │   │   └── views/              # HTML graph generation logic
│   │   │   └── config_files/           # Example .conf files
│   │   └── java/html_files/            # Static web assets (HTML/CSS)
│   └── test/java/test/                 # Unit tests (Server and Logic)
├── pom.xml                             # Maven configuration
└── README.md                           # Project documentation
```

## Environment Variables

- None currently used. TODO: Add support for configurable port via `PORT` environment variable.

## Tests

The project uses JUnit 5. To run tests:
```powershell
mvn test
```
Tests cover:
- HTTP Server functionality.
- Request parsing.
- Core project logic.

## License

TODO: Add license information (e.g., MIT, Apache 2.0).