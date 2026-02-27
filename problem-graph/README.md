# problem-graph

Composable action graph with expectations and dispatching utilities for Kotlin Multiplatform (JVM + Native).

This module helps you model a computation as a graph of Actions with:
- Explicit execution lifecycle (exec/completed/throwable/result)
- A dispatcher to submit and resolve actions
- Expectations (validators) for results
- Optional sub-actions that can be scheduled alongside primary work
- Simple helpers (v1) for ergonomic result retrieval and optional blocks


## Targets
- Kotlin/JVM
- Kotlin/Native (host-dependent target is selected at build time)


## Coordinates
Group and version are inherited from the root project:
- group: `io.github.kshulzh.kshku`
- version: `0.0.1-SNAPSHOT` (see gradle.properties)
- module: `problem-graph`

Maven coordinates (when published):
- `io.github.kshulzh.kshku:problem-graph:<version>`

For local development you can publish to your local Maven repo and then depend on it:
- gradlew.bat :problem-graph:publishToMavenLocal  (Windows)
- ./gradlew :problem-graph:publishToMavenLocal    (Linux/macOS)

Gradle (Kotlin DSL) usage example after local publish:

dependencies {
    implementation("io.github.kshulzh.kshku:problem-graph:0.0.1-SNAPSHOT")
}


## Installation in a KMP project
In your module's build.gradle.kts:

kotlin {
    jvm()
    // add native targets as needed
}

dependencies {
    commonMainImplementation("io.github.kshulzh.kshku:problem-graph:0.0.1-SNAPSHOT")
}

Note: This repo uses Kotlin 2.1.x and depends on kotlinx-coroutines and kotlinx-serialization.


## Core concepts

- Action<T>: a unit of work with lifecycle state and a result.
  - Properties: `throwable`, `completed`, `result`
  - Method: `fun exec()` to perform the action's work
  - Extension: `finished()` convenience check

- NodeActionImpl<T>: a concrete, composable action that can:
  - Maintain observers, expectations, optional sub-actions, and subnodes
  - Execute a handler block and validate the produced value via expectations

- Dispatcher: ActionDispatcher submits and executes actions; ActionDispatcherImpl provides a simple queue-based implementation.

- Expectations: Expect<T> validates values and can be combined; sample implementations like NotNullExpect and MultiExpect are provided.

- v1 helpers: utilities for retrieving results and scheduling optional work ergonomically (see below).


## Quick start

The simplest pattern is to create a dispatcher and a ProblemContext, build a node action, submit it, and resolve.

import io.github.kshulzh.problemgraph.action.NodeActionImpl
import io.github.kshulzh.problemgraph.context.ActionDispatcherImpl
import io.github.kshulzh.problemgraph.context.ProblemContext

val dispatcher = ActionDispatcherImpl()
val ctx = ProblemContext(dispatcher, dispatcher)

val node = NodeActionImpl(ctx) {
    // your computation here
    42
}

dispatcher.submit(node)
dispatcher.resolve()
val result = node.result // 42

This mirrors the test `SingleNodeTests.one node success`.


### Handling failures
If the handler throws, `result` access will throw that exception, and the dispatcher will keep the failing action in its queue:

val failing = NodeActionImpl<Int>(ctx) { error("Boom") }
dispatcher.submit(failing)
val resolution = dispatcher.resolve()
println(resolution.queue.size) // 1


### Optional sub-actions (v1)
Use optional blocks to schedule additional Unit actions connected to a node.

import io.github.kshulzh.problemgraph.v1.optional

var counter = 0
val nodeWithOptional = NodeActionImpl(ctx) {
    this.optional {
        counter += 1
    }
    1
}

dispatcher.submit(nodeWithOptional)
nodeWithOptional.exec() // schedules optional
dispatcher.resolve()    // processes queued optional work
nodeWithOptional.exec() // after optional completed, main action can proceed again
println(counter) // 1

This mirrors the test `SingleNodeTests.one node with optional`.


### Expectations
Define expectations to validate results and fail fast when values do not satisfy constraints.

import io.github.kshulzh.problemgraph.expect.Expect

class PositiveExpect : Expect<Int> {
    override fun expected(value: Int): Throwable? =
        if (value > 0) null else IllegalArgumentException("Expected positive")
}

val nodeWithExpect = NodeActionImpl(ctx) {
    10
}.also { action ->
    // Attach expectations through ExpectableAction APIs if desired
    // Alternatively, validate in handler and throw explicitly
}

// When exec runs, expectations can be applied using the provided helpers.


## v1 helpers overview
Convenience utilities live in `io.github.kshulzh.problemgraph.v1`:
- optional { ... } and optional(key) { ... } to enqueue optional Unit actions on a NodeActionImpl
- calc/get helpers to evaluate/obtain results from actions with optional Expect sets

Examples (conceptual):

import io.github.kshulzh.problemgraph.v1.calc
import io.github.kshulzh.problemgraph.v1.get

// Given an Action<Int> a
val value = get(a) // ensures it is dispatched if needed and returns the action itself
val result = calc(a) // returns Int, validating via attached expectations


## Running tests (module only)
- gradlew.bat :problem-graph:test  (Windows)
- ./gradlew :problem-graph:test    (Linux/macOS)

See tests under `src/commonTest/kotlin/...` for practical usage patterns:
- it/SingleNodeTests.kt
- it/TwoNodeTests.kt
- it/ThreeNodeTests.kt
- expect/ExpectTest.kt


## License
Apache License 2.0 (see repository root LICENSE)

---
Last updated: 2026-02-21
