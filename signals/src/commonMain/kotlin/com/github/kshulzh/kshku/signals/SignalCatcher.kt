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

import com.github.kshulzh.kshku.local.thread.ThreadLocalContext
import kotlin.reflect.KClass

/**
 * Represents an interface for catching and handling signals in a thread-local context.
 * The `SignalCatcher` interface provides a mechanism for registering signal handlers for specific types
 * and propagating signals to corresponding handlers during program execution.
 */
interface SignalCatcher {
    /**
     * Provides a companion object for the SignalCatcher interface. This companion object holds
     * a thread-local reference to the current active SignalCatcher instance.
     *
     * The SignalCatcher acts as a mediator for handling signals within a thread by maintaining
     * a contextual signal catcher. Signal handling functions rely on this companion object to
     * determine the active signal catcher in the current thread context.
     *
     * The thread-local reference ensures isolation between different threads, enabling
     * thread-safe operations and independent signal handlers per thread.
     *
     * @property signal The thread-local reference to the current active SignalCatcher instance.
     * It can be set or retrieved to manage the context of signal handling within the current thread.
     */
    companion object {
        /**
         * A thread-local variable used to manage the current `SignalCatcher` instance for handling signals.
         *
         * This property allows the association of a `SignalCatcher` instance with the current thread
         * and provides mechanisms to manage and propagate signals. It ensures thread-safe operations
         * by using a `ThreadLocalContext` for storing and accessing the `SignalCatcher` instance.
         *
         * Changes to this variable affect the signal handling context of the current thread.
         * If unset, the default behavior is to propagate signals to the parent `SignalCatcher`,
         * or ignore them if no parent is available.
         */
        var signal by ThreadLocalContext<SignalCatcher>()::value
    }
    /**
     * Registers a catcher for a specific class type [kClass] and associates a handler function [body] with it.
     *
     * The handler function [body] will be executed whenever an object of the registered type is handled.
     * Allows chaining catchers for various types to implement signal handling logic.
     *
     * @param kClass The class type for which the catcher is registered.
     * @param body A lambda function to handle objects of the specified type [kClass].
     *             The function accepts an instance of the type [T] as its parameter.
     */
    fun <T : Any> addCatcher(kClass: KClass<*>, body: (T)->Unit)
    /**
     * Handles an object of type [T] by invoking the corresponding signal handler, if available.
     *
     * This method processes the given object by finding a registered signal handler
     * that matches the type of the object. If a matching handler is not found,
     * the signal is propagated to the parent signal catcher (if one exists).
     *
     * @param obj The object of type [T] to be handled. The type [T] represents
     * a signal for which a handler might be registered.
     */
    fun <T> handle(obj: T)
    /**
     * Configures the current signal catcher as the active catcher in the current scope.
     *
     * This method sets the current instance of `SignalCatcher` as the active signal catcher
     * by assigning it to the thread-local `signal` property. The previous active signal catcher
     * (if any) is stored as the parent signal catcher, allowing signals to propagate to it
     * if no matching handler is found in the current catcher.
     *
     * This setup allows for nested signal handling, where each signal catcher can capture
     * and handle signals independently before delegating unhandled signals to the parent catcher.
     */
    fun setup()
    /**
     * Removes the current signal catcher from the active context.
     *
     * This method restores the parent signal catcher (if any) by setting it as the active
     * signal catcher. If no parent signal catcher exists, the active context becomes null.
     * This ensures proper cleanup of the signal catching hierarchy, thus preventing potential
     * memory leaks or conflict between signal handlers.
     */
    fun delete()
}

/**
 * Registers a signal catcher for a specific type [T] within the current `SignalCatcher` instance.
 *
 * The handler function [body] is executed whenever a signal of type [T] is emitted and caught within
 * the context of this `SignalCatcher`. This method modifies the current `SignalCatcher` by adding a new
 * catcher for type [T].
 *
 * @param body The function to handle signals of type [T]. This function takes an instance of type [T] as its parameter.
 * @return The same `SignalCatcher` instance, allowing further chaining of signal registrations.
 */
inline infix fun <reified T : Any> SignalCatcher.signal(noinline body: (T)->Unit) : SignalCatcher{
    return this.also {
        addCatcher(T::class, body)
    }
}

/**
 * Executes the given [body] function within the context of this `SignalCatcher`,
 * ensuring proper initialization and cleanup of signal handling.
 *
 * The `setup` method is called before invoking the [body] function to set up the signal catcher,
 * and the `delete` method is called afterwards to restore the previous state, even in the
 * case of exceptions.
 *
 * @param body The block of code to execute within the context of this `SignalCatcher`.
 * @return The result of the execution of the [body] function.
 * @throws Any exceptions thrown by the [body] function.
 */
inline fun <T>SignalCatcher.run(body: ()->T) : T {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}

/**
 * Executes the given function [body] within the context of the current `SignalCatcher`,
 * setting up the signal catcher before execution and ensuring cleanup afterward.
 *
 * @param body The function to be executed. It is allowed to return a nullable result of type [T].
 * @return The result of the executed function [body], or null if the function returns null.
 */
inline fun <T>SignalCatcher.runNullable(body: ()->T?) : T? {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}