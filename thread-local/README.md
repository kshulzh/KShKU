# thread-local

Cross-platform Kotlin Multiplatform utilities for lightweight thread/coroutine-local context storage.

This module exposes simple abstractions to keep scoped values without platform-specific ThreadLocal APIs:
- ThreadLocalContext<T> — JVM/Native simple thread-local delegate.
- CoroutineLocalContextImpl<T> — coroutine Job-scoped storage.
- CoroutineLocalContextHierarchical<T> — Job-scoped storage with parent lookup (hierarchical resolution).

Targets: JVM and Kotlin/Native (host-dependent native target is selected at build time).


## Coordinates
Inherited from the root project:
- group: `io.github.kshulzh.kshku`
- version: see repository root `gradle.properties`
- module: `thread-local`

Maven coordinates (when published):
- `io.github.kshulzh.kshku:thread-local:<version>`

Local publish for development:
- Windows: `gradlew.bat :thread-local:publishToMavenLocal`
- Linux/macOS: `./gradlew :thread-local:publishToMavenLocal`

Gradle (Kotlin DSL) usage after local publish:

dependencies {
    implementation("io.github.kshulzh.kshku:thread-local:0.0.1-SNAPSHOT")
}


## Installation in a KMP project
In your module's build.gradle.kts:

kotlin {
    jvm()
    // add native targets as needed
}

dependencies {
    commonMainImplementation("io.github.kshulzh.kshku:thread-local:<version>")
}

Notes:
- Coroutine APIs rely on kotlinx-coroutines. JVM ThreadLocal usage is encapsulated.


## Quick start: thread-local delegate
Store per-thread state using the property delegate:

import io.github.kshulzh.kshku.local.thread.ThreadLocalContext

class SessionHolder {
    var current by ThreadLocalContext<String>()::value
}

val holder = SessionHolder()
holder.current = "user-1"
println(holder.current) // user-1


## Quick start: coroutine-local hierarchical
Keep values bound to a coroutine Job and allow parent lookup:

import io.github.kshulzh.kshku.local.coroutine.CoroutineLocalContextHierarchical
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch

val ctx = CoroutineLocalContextHierarchical<String>()

runBlocking {
    ctx.setValue("parent")
    println(ctx.getValue()) // parent

    launch {
        // not set in child yet; hierarchical lookup returns parent
        println(ctx.getValue()) // parent
        ctx.setValue("child")
        println(ctx.getValue()) // child
    }.join()

    // After child completes, parent remains unchanged
    println(ctx.getValue()) // parent
}


## Running tests (module only)
- Windows: `gradlew.bat :thread-local:test`
- Linux/macOS: `./gradlew :thread-local:test`


## License
Apache License 2.0 (see repository root LICENSE)

---
Last updated: 2026-02-21
