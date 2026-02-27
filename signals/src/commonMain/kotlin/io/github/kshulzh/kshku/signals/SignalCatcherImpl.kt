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

import kotlin.reflect.KClass

/**
 * Implementation of the `SignalCatcher` interface. This class provides mechanisms to register
 * and handle signals for specific object types and supports nested signal catchers by leveraging
 * a parent-child relationship. Signals can be propagated to a parent catcher when no
 * matching handler is found in the current instance.
 */
class SignalCatcherImpl : SignalCatcher {
    /**
     * Represents the parent signal catcher in a hierarchy of signal catchers.
     *
     * The `parent` property allows the propagation of unhandled signals to its parent catcher.
     * If the current instance cannot handle a signal, the signal is forwarded to the `parent`,
     * enabling a chain of responsibility pattern for signal handling. The parent catcher can
     * either process the signal or further propagate it to its own parent.
     *
     * The value of `parent` is typically set during the setup of the signal catcher hierarchy.
     * It can be `null` if there is no parent catcher available, indicating the root of the hierarchy.
     */
    var parent: SignalCatcher? = null
    /**
     * A mutable map that associates a [KClass] with a handler function.
     *
     * This map is used internally to store signal catchers and their corresponding handler functions.
     * Each key in the map represents a class type, and the associated value is a function to handle signals
     * of that specific type. Signal handlers are invoked when signals of the matching type are emitted.
     */
    val map:MutableMap<KClass<*>, (Any) -> Unit> = linkedMapOf()
    /**
     * Registers a handler function for signals of a specific type [T].
     *
     * This function allows associating a handler function [body] with a specific class type [kClass].
     * The handler function will be invoked when a signal of the specified type is emitted.
     *
     * @param T The type of the signal to be caught. Should extend [Any].
     * @param kClass The [KClass] of the type [T] for which the handler is registered.
     * @param body The handler function to be executed when a signal of type [T] is caught.
     * It takes an instance of [T] as input.
     */
    override fun <T : Any> addCatcher(kClass: KClass<*>, body: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        map[kClass] = body as (Any) -> Unit
    }

    /**
     * Handles an object of type [T] by finding and invoking the corresponding signal handler.
     *
     * This method attempts to locate a registered handler function based on the runtime type
     * of the given object. If a matching handler is found, it is invoked with the object as its parameter.
     * If no handler is available for the object's type, the signal is passed to the parent
     * signal catcher, if one is defined.
     *
     * @param obj The object of type [T] to be processed by the signal handler. The type [T]
     * represents the signal that the handler is designed to process.
     * If no handler exists for the type of [obj], the signal may propagate to the parent.
     */
    override fun <T> handle(obj: T) {
        map.entries.find {
            it.key.isInstance(obj)
        }?.value?.let { it(obj as Any) } ?: parent?.handle(obj)
    }

    /**
     * Sets up the current instance as the active signal catcher.
     *
     * This method assigns the current active signal catcher to the `parent` property of the current instance
     * and then updates the global signal catcher reference to point to the current instance.
     *
     * The setup process allows for chaining and nesting of signal catchers, ensuring that signals can propagate
     * to the parent signal catcher if the current instance does not provide a matching handler.
     */
    override fun setup() {
        parent = SignalCatcher.signal
        SignalCatcher.signal = this
    }

    /**
     * Restores the previous state of the signal catcher by reassigning the active catcher to its parent.
     *
     * This method sets the active signal catcher (`SignalCatcher.signal`) to the parent catcher,
     * effectively removing the current signal catcher from the active signal handling chain.
     * It is used to clean up or deregister the current signal catcher.
     */
    override fun delete() {
        SignalCatcher.signal = parent
    }
}