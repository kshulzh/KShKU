# KShKU

A Kotlin Multiplatform (KMP) monorepo containing small, reusable libraries:
- thread-local — cross-platform thread/coroutine local context utilities.
- signals — simple signal/observer utilities with coroutine support.
- problem-graph — composable action graph with expectations and dispatching utilities.

This repository is organized as a Gradle multi-module build.

## Tech stack
- Language: Kotlin (Multiplatform) — Kotlin 2.3.x
- Build tool: Gradle (wrapper included)
  - Gradle wrapper: 8.10 (see gradle/wrapper/gradle-wrapper.properties)
- Targets: JVM and Kotlin/Native (host-dependent target selected at build time)
## Modules overview

### thread-local
Cross-platform utilities for local context in threads and coroutines.
- Targets: JVM + Native (host OS auto-detected in build scripts)

### signals
Signal/observer helpers with coroutine-friendly APIs.
- Depends on: thread-local
- Contains a sample `main()` function (see `signals/src/commonMain/.../Main.kt`) demonstrating usage.
- Note: The Application plugin is not configured; see the Run section for how to execute.

### problem-graph
Composable actions graph with expectations and dispatching utilities.
- Key packages: `action`, `context`, `expect`, `delegate`, `v1`
- Tests demonstrate usage (see `problem-graph/src/commonTest/...`)

## Requirements
- Java JDK: TODO(Confirm required version; Kotlin 2.1 typically runs well on JDK 17+)
- Git
- Internet access to fetch dependencies
- OS: Windows/Linux/macOS (Native target is selected dynamically; some targets require a compatible host toolchain)

Gradle wrapper is included, so no local Gradle installation is required.

## Getting started

Clone the repository:
- git clone https://github.com/kshulzh/KShKU.git
- cd KShKU

Build all modules:
- On Windows: gradlew.bat build
- On Linux/macOS: ./gradlew build

Run all tests:
- On Windows: gradlew.bat test
- On Linux/macOS: ./gradlew test

Build a single module (example: problem-graph):
- gradlew.bat :problem-graph:build

Run tests for a single module:
- gradlew.bat :signals:test

Publish to local Maven (for development/testing):
- gradlew.bat publishToMavenLocal

## Running code (entry points)
The repository is a library-first setup. There is a demonstrative entry point in the `signals` module:
- File: `signals/src/commonMain/kotlin/com/github/kshulzh/kshku/signals/Main.kt`
- Function: `fun main()`

Ways to run it:
- In IDE (IntelliJ IDEA): open the project, locate the file, and run the `main()` function.
- Gradle task: TODO(Add application plugin or `JavaExec` task for JVM to run `:signals` main from Gradle.)

Kotlin/Native binaries are not explicitly configured to produce executables in the current build scripts; the target is created but no run task is exposed. Library artifacts are still built for the configured targets.

## Common Gradle tasks
- build — compiles all targets
- test — runs tests across modules
- publishToMavenLocal — publishes modules to local Maven repo
- :<module>:build — builds a specific module
- :<module>:test — runs tests for a module
- :<module>:publish — publishes a module (if a repository is configured)

## Tests
All modules define `commonTest` (and for JVM, `jvmTest`) source sets using Kotlin Test.
Run all tests:
- gradlew.bat test

Run tests for a module:
- gradlew.bat :problem-graph:test

Example test locations:
- problem-graph/src/commonTest/kotlin/...
- signals/src/commonTest/kotlin/... (if any are added)

## Project structure
- build.gradle.kts — Root Gradle build
- settings.gradle.kts — Includes subprojects: thread-local, signals, problem-graph
- gradle/libs.versions.toml — Version catalog (Kotlin 2.1, Coroutines, Serialization, Ktor, etc.)
- thread-local/ — KMP module for local contexts (JVM + Native)
- signals/ — KMP module for signal/observer utilities (depends on thread-local); includes a sample main()
- problem-graph/ — KMP module for action graphs and expectations
- LICENSE — Apache License 2.0

## License
This project is licensed under the Apache License 2.0. See the LICENSE file for details.

## Roadmap / TODOs
- Add JVM application plugin or `JavaExec` task to run `signals` main via Gradle.
- Confirm and document the minimum required JDK version.
- Add publishing repositories for modules other than `thread-local`, or document the intended distribution strategy.
- Provide usage examples and API docs for each module.
- Consider configuring multiplatform executables for Native targets where applicable.
