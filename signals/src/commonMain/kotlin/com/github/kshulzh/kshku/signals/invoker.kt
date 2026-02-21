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
 * Emits a signal of the specified type to be handled by a corresponding signal catcher.
 *
 * The generic type [T] defines the type of the object being signaled. This method uses
 * the currently active `SignalCatcher` (if any) to handle the object passed as an argument.
 * If no signal catcher is active or a matching handler is not found, the signal is ignored.
 *
 * @param obj The object of type [T] to be handled by the signal catcher.
 */
inline fun <reified T : Any> signal(obj: T) {
    SignalCatcher.signal?.handle(obj)
}

/**
 * Sends a signal of type [T] within a coroutine context.
 * The signal will be handled by the matching catcher function registered in the coroutine signal catcher.
 *
 * @param obj The object of type [T] to be signaled and handled.
 */
suspend inline fun <reified T : Any> signalSuspend(obj: T) {
    CoroutineSignalCatcher.signal.getValue()?.handle(obj)
}



