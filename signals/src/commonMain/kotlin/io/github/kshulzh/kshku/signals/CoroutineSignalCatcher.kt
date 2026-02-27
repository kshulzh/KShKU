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

package io.github.kshulzh.kshku.signals

import io.github.kshulzh.kshku.local.coroutine.CoroutineLocalContextHierarchical
import kotlin.reflect.KClass

/**
 * Interface that defines a coroutine-aware signal catcher system.
 * It allows registering signal handlers for specific types and
 * provides methods for handling dispatched signals based on type.
 */
interface CoroutineSignalCatcher {
    /**
     * Companion object that provides a shared context for managing instances of CoroutineSignalCatcher.
     * This allows hierarchical and context-aware management of coroutine signal handling.
     */
    companion object {
        /**
         * Represents a hierarchical coroutine-local context that holds the current instance of
         * the [CoroutineSignalCatcher] within a coroutine scope.
         *
         * This variable is used to maintain and propagate the coroutine-local state of signal handlers, ensuring
         * that handlers registered in a parent coroutine can be accessed by child coroutines if no direct handler
         * is available. The hierarchical nature of this context allows dynamic resolution of signal handlers across
         * coroutine hierarchies. It is primarily utilized within the [CoroutineSignalCatcher] framework for setting, getting,
         * and managing signal handlers during coroutine execution.
         *
         * The underlying implementation leverages a map-based storage structure for associating signal handlers
         * with coroutine contexts, supporting efficient resolution of handlers based on the active coroutine hierarchy.
         */
        var signal = CoroutineLocalContextHierarchical<CoroutineSignalCatcher>()
    }
    /**
     * Adds a catcher for handling signals of a specific type.
     *
     * @param T The type of the signal that this catcher will handle. The type must be a non-nullable type.
     * @param kClass The KClass of the signal type to be handled.
     * @param body The suspendable function to execute when a signal of the specified type is received.
     */
    fun <T : Any> addCatcher(kClass: KClass<*>, body: suspend (T)->Unit)
    /**
     * Handles a given object of type [T] by invoking the corresponding catcher function if defined.
     *
     * @param obj The object of type [T] to be handled. This object will be matched against registered catcher functions,
     * and the corresponding function will be invoked if a match is found.
     */
    suspend fun <T> handle(obj: T)
    /**
     * Prepares the coroutine signal catching context by setting up the current signal catcher
     * instance as the active one. This allows signals to be handled using the current catcher
     * during its execution.
     *
     * This method updates the parent reference to retain the existing signal catcher, ensuring
     * that the previous signal catcher can be restored after the current one is no longer
     * active.
     *
     * Suspending function, must be called within a coroutine.
     *
     * Used in conjunction with `delete` to properly manage the lifecycle of signal catchers
     * within a coroutine context.
     */
    suspend fun setup()
    /**
     * Removes the current coroutine signal catcher from the execution context, restoring the previous state.
     * This method is intended to be invoked after a setup process to ensure proper cleanup of the signal-catching context.
     *
     * This operation is typically used in conjunction with the `setup` method, maintaining the lifecycle
     * of the coroutine signal catcher.
     *
     * It is recommended to call this method within a `finally` block or similar construct to
     * guarantee that the context is always restored, even in the event of exceptions.
     *
     * @receiver CoroutineSignalCatcher The context that should be deleted from the signal-catching chain.
     */
    suspend fun delete()
}

/**
 * Registers a handler for a signal of the specified type `T`. When a signal of type `T` is emitted,
 * the provided `body` coroutine will be executed.
 *
 * @param T The type of the signal to handle.
 * @param body A suspendable function to execute when a signal of type `T` is emitted. The function
 *        takes the signal instance as a parameter.
 * @return The current `CoroutineSignalCatcher` instance, allowing for method chaining.
 */
inline infix fun <reified T : Any> CoroutineSignalCatcher.signal(noinline body: suspend (T)->Unit) : CoroutineSignalCatcher{
    return this.also {
        addCatcher(T::class, body)
    }
}

/**
 * Executes the provided body of code within the context of the current `CoroutineSignalCatcher`,
 * ensuring that the `setup` method is called before execution and the `delete` method is called
 * after execution, even if an exception occurs.
 *
 * @param T The return type of the provided body of code.
 * @param body A lambda function representing the body of code to execute.
 * @return The result of executing the provided body of code.
 */
suspend inline fun <T>CoroutineSignalCatcher.run(body: ()->T) : T {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}

/**
 * Executes the provided nullable block within the context of the `CoroutineSignalCatcher`,
 * ensuring proper setup and cleanup of the signal catcher.
 *
 * The `setup` method of the `CoroutineSignalCatcher` is invoked before the block runs,
 * and the `delete` method is invoked afterwards, maintaining the integrity of the signal-catching mechanism.
 *
 * @param T The type of the result produced by the given block.
 * @param body The nullable block of code to be executed.
 * @return The result of the executed block, or `null` if the block returns `null`.
 */
suspend inline fun <T>CoroutineSignalCatcher.runNullable(body: ()->T?) : T? {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}