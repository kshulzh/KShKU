# signals

Kotlin Multiplatform utilities for lightweight signal/observer patterns with optional coroutine awareness.

This module provides two small, composable catcher abstractions:
- SignalCatcher — thread-local signal routing with a simple push/pop scope.
- CoroutineSignalCatcher — coroutine-local routing with hierarchical lookup across parent/child jobs.

Targets: JVM and Kotlin/Native (host-dependent native target is selected at build time).


## Coordinates
Inherited from the root project:
- group: `io.github.kshulzh.kshku`
- version: see repository root `gradle.properties`
- module: `signals`

Maven coordinates (when published):
- `io.github.kshulzh.kshku:signals:<version>`

For local development you can publish to your local Maven repo and then depend on it:
- Windows: `gradlew.bat :signals:publishToMavenLocal`
- Linux/macOS: `./gradlew :signals:publishToMavenLocal`

Gradle (Kotlin DSL) usage after local publish:

dependencies {
    implementation("io.github.kshulzh.kshku:signals:0.0.1-SNAPSHOT")
}


## Installation in a KMP project
In your module's build.gradle.kts:

kotlin {
    jvm()
    // add native targets as needed
}

dependencies {
    commonMainImplementation("io.github.kshulzh.kshku:signals:<version>")
}

Notes:
- Depends on kotlinx-coroutines and the sibling module `thread-local`.


## Quick start (thread-local)
Use SignalCatcher for simple thread-scoped routing:
```kotlin

SignalCatcherImpl()
            .signal<Int> {
                println("int $it")
            }
            .signal<String> {
                println("string $it")
            }.run {
                fun1()
            }
// Outside the scope no catcher is installed
```

You can nest scopes. If a child catcher has no matching handler, the signal is delegated to the parent.


## Quick start (coroutines + hierarchy)
CoroutineSignalCatcher stores context per Job and looks up parents when no local handler is present.

import io.github.kshulzh.kshku.signals.CoroutineSignalCatcherImpl
import io.github.kshulzh.kshku.signals.signal
import io.github.kshulzh.kshku.signals.signalSuspend
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch

runBlocking {
    var handled = 0
    val parent = CoroutineSignalCatcherImpl().signal<Int> { handled += it }

    parent.run {
        // Install a child catcher that handles other types
        val child = CoroutineSignalCatcherImpl().signal<String> { /* ignored in this example */ }
        child.run {
            // Emit Int from a child coroutine; child has no Int handler, so it delegates to parent
            launch { signalSuspend(3) }.join()
        }
    }
    println(handled) // 3
}


## API overview
- signal<T>(body) top-level helper creates a new catcher and registers handler for T.
- SignalCatcher.signal<T>(body) registers a handler on an existing catcher (fluent).
- SignalCatcher.run { ... } installs the catcher for the duration of the block (push/pop).
- signal(value) emits to the current thread-local catcher.

Coroutine variants:
- signalSuspend<T>(body) creates a CoroutineSignalCatcher and registers a handler.
- CoroutineSignalCatcher.signal<T>(body) registers a suspend handler.
- CoroutineSignalCatcher.run { ... } installs catcher in the current coroutine Job.
- signalSuspend(value) emits to the current coroutine catcher; lookup walks parent jobs.

See tests under `src/commonTest` for more examples.


## Running tests (module only)
- Windows: `gradlew.bat :signals:test`
- Linux/macOS: `./gradlew :signals:test`


## License
Apache License 2.0 (see repository root LICENSE)

---
Last updated: 2026-02-21
