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

package io.github.kshulzh.problemgraph.context

import io.github.kshulzh.problemgraph.action.Action

/**
 * Manages the lifecycle and state of actions within a system. The `ActionManager` interface is responsible
 * for handling `Action` objects, including their submission and resolution processes. It ensures
 * the execution and coordination of actions and provides a mechanism to evaluate the current state
 * of the action management system.
 */
interface ActionManager {
    /**
     * Submits an action to be managed or executed by the action manager.
     *
     * This method accepts an action which encapsulates a specific task or operation.
     * The action is processed in a manner specific to the implementation of the `ActionManager`.
     * Typically, this may involve tracking its lifecycle, managing execution, or integrating it into
     * a broader context or workflow.
     *
     * @param action The action to be submitted. This action represents a task or operation and
     *               contains its execution logic, result, completion status, and any
     *               exceptions encountered during its execution.
     */
    fun submit(action: Action<*>)
    /**
     * Resolves the current state and provides the resulting resolution data.
     *
     * This method collects and processes the necessary actions and their current execution queue,
     * consolidating them into a `ResolutionResult`. The result contains a list of actions
     * to be addressed as well as the current state of the action queue.
     *
     * @return A `ResolutionResult` object that encapsulates a list of actions and their associated queue,
     *         representing the resolution state at the time of invocation.
     */
    fun resolve() : ResolutionResult
}