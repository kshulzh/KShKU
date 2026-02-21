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

package com.github.kshulzh.problemgraph.context

import com.github.kshulzh.problemgraph.action.Action
import com.github.kshulzh.problemgraph.action.finished

/**
 * Represents a contract for dispatching actions within a system.
 * Implementations of this interface are responsible for handling and executing actions
 * of type [Action], managing their execution lifecycle, and optionally reacting to their outcomes.
 */
interface ActionDispatcher {
    /**
     * Dispatches the given action for execution, managing its lifecycle and state.
     *
     * This method ensures that the provided `Action` is executed and its completion
     * status or exceptions are handled appropriately. If the action has not yet finished,
     * it will be executed. Depending on its current state (e.g., completed, pending, or failed),
     * further logic may be applied to retry the action, notify observers, or update its dependencies.
     *
     * @param action The action to be dispatched which encapsulates a specific task or operation
     *               to manage and execute. This action may include its result, completion state,
     *               and any associated exceptions.
     */
    fun dispatch(action: Action<*>)
}

/**
 * Dispatches the provided action if it has not already finished execution.
 *
 * This method checks whether the given action is already finished (either completed or encountered an error)
 * by using the `finished` method of the `Action` interface. If the action is not finished, it dispatches the action
 * using the `dispatch` method of the `ActionDispatcher` interface.
 *
 * @param action The action to be processed. If the action is not finished, it will be dispatched.
 */
fun ActionDispatcher.dispatchIfNotFinished(action: Action<*>) {
    if (!action.finished()) {
        dispatch(action)
    }
}