/*
 * Copyright (c) 2026. Kirill Shulzhenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.kshulzh.kshku.signals

import kotlin.reflect.KClass

/**
 * An implementation of the `CoroutineSignalCatcher` interface that provides
 * a coroutine-aware mechanism for handling signals of specific types.
 *
 * This class maintains a mapping of signal types to their corresponding handler functions
 * and supports hierarchical signal handling by delegating to a parent signal catcher
 * if no appropriate handler is found in the current instance.
 */
class CoroutineSignalCatcherImpl : CoroutineSignalCatcher {
    /**
     * Represents the parent signal catcher in a hierarchical coroutine signal-catching system.
     *
     * This property allows propagation of unhandled signals to the parent `CoroutineSignalCatcher` instance,
     * enabling a chain of responsibility pattern. When the current instance cannot process a signal,
     * it delegates the handling to its parent, if available. This ensures that signals can be handled
     * by appropriate catchers higher in the hierarchy.
     *
     * The parent is typically set during the setup phase of a `CoroutineSignalCatcher`,
     * and is restored upon deletion of the current context.
     */
    var parent: CoroutineSignalCatcher? = null
    /**
     * A mutable map storing signal handlers for different types.
     *
     * Each entry in the map associates a type (represented by its corresponding [KClass])
     * with a suspendable function that handles objects of that type. The suspendable function
     * accepts an instance of the associated type as its argument and performs some operation.
     *
     * This map is used internally to register and manage signal handlers in a type-safe manner.
     * Handlers can be invoked to execute specific logic for the provided object type.
     *
     * The map is initialized as a [linkedMapOf], preserving the insertion order of keys.
     */
    val map:MutableMap<KClass<*>, suspend (Any) -> Unit> = linkedMapOf()
    /**
     * Registers a catcher for a specific class type, allowing for the handling of signals or events of that type.
     *
     * @param T The type parameter defining the expected type of the signal or event.
     * @param kClass The class type for which the catcher is being registered.
     * @param body A suspendable lambda function to be executed when a signal of the specified type is handled.
     */
    override fun <T : Any> addCatcher(kClass: KClass<*>, body: suspend (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        map[kClass] = body as suspend (Any) -> Unit
    }

    /**
     * Handles a given object of type [T] by invoking the corresponding catcher function if it is defined.
     * If no matching catcher function is found in the current instance, the handling is delegated to the parent catcher.
     *
     * @param obj The object of type [T] to be handled. This object is matched against registered types, and the corresponding
     * catcher function is invoked if a match is found. If no match is found, the parent catcher's `handle` method is called, if a parent exists.
     */
    override suspend fun <T> handle(obj: T) {
        map.entries.find {
            it.key.isInstance(obj)
        }?.value?.let { it(obj as Any) } ?: parent?.handle(obj)
    }

    /**
     * Initializes the current `CoroutineSignalCatcherImpl` instance as the active signal catcher
     * in the current coroutine context. It sets the `parent` property to the value of the previously
     * active signal catcher and replaces it with the current instance.
     *
     * This method should be invoked to establish the current instance as the active context for
     * catching coroutine signals. The previous state of the signal catcher is stored in the
     * `parent` property, allowing for restoration or chaining of signal handlers if necessary.
     *
     * The operation relies on `CoroutineSignalCatcher.signal` for accessing and modifying the
     * active signal catcher in the context, using its `getValue` and `setValue` methods.
     *
     * This method should be used in conjunction with the `delete()` method to properly
     * manage the lifecycle of the signal catcher, ensuring the correct restoration of the
     * previous active signal catcher state.
     */
    override suspend fun setup() {
        parent = CoroutineSignalCatcher.signal.getValue()
        CoroutineSignalCatcher.signal.setValue(this)
    }

    /**
     * Restores the parent context of the current `CoroutineSignalCatcher` instance.
     *
     * This method removes the current instance from the context stack by re-assigning
     * the parent `CoroutineSignalCatcher` as the current one in the system-wide signal handler.
     * It is typically called after signal handling is completed to clean up and revert
     * to the previous state.
     *
     * This operation ensures that the signal handling context is correctly maintained
     * throughout the lifecycle of coroutines utilizing signal handling functionality.
     */
    override suspend fun delete() {
        CoroutineSignalCatcher.signal.setValue(parent)
    }
}