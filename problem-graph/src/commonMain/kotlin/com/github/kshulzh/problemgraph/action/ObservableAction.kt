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

package com.github.kshulzh.problemgraph.action

/**
 * Represents an observable action that allows tracking and managing observers associated
 * with the action's execution lifecycle.
 *
 * An `ObservableAction` maintains a list of observers, where each observer is an instance
 * of `Action<*>`. These observers can monitor the execution progress, completion, or failure
 * of the associated `ObservableAction`, enabling flexible workflows and dependency tracking.
 */
interface ObservableAction {
    /**
     * A mutable collection of observers associated with this `ObservableAction`.
     *
     * Observers are instances of `Action<*>` that can track the lifecycle or execution
     * state of this `ObservableAction`. These observers may be notified or updated
     * based on specific changes or events within the associated action.
     *
     * This list allows dynamic addition and removal of observers at runtime,
     * providing flexibility in managing dependencies and event propagation
     * during the execution of actions. Observers often play a critical role
     * in orchestrating workflows or handling side effects triggered by actions.
     */
    val observers: MutableList<Action<*>>
}