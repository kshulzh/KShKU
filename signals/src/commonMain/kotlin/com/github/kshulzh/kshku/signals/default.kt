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

/**
 * Registers a signal catcher for a specific type [T] and associates it with a handler function.
 *
 * The handler function [body] is invoked whenever a signal of type [T] is emitted.
 * This method creates a signal catcher and registers the handler for the specified type [T].
 * Signal catchers can be nested, and signals are propagated to the parent signal catcher if no match is found.
 *
 * @param body The handler function executed when a signal of type [T] is caught.
 * It accepts an object of type [T] as its parameter.
 * @return A new instance of [SignalCatcher] configured with the specified handler.
 */
inline fun <reified T : Any> signal(noinline body: (T)->Unit) : SignalCatcher{
    return SignalCatcherImpl().also {
        it.addCatcher(T::class, body)
    }
}

/**
 * Creates and returns a `CoroutineSignalCatcher` instance after registering a signal handler
 * for the specified type. The handler is a suspendable function that will be invoked when
 * a signal of the specified type is received.
 *
 * @param T The type of signal to be handled. Must be a non-nullable type.
 * @param body A suspendable lambda function to handle signals of type `T`.
 * @return An instance of `CoroutineSignalCatcher` with the registered signal handler.
 */
inline fun <reified T : Any> signalSuspend(noinline body: suspend (T)->Unit) : CoroutineSignalCatcher{
    return CoroutineSignalCatcherImpl().also {
        it.addCatcher(T::class, body)
    }
}