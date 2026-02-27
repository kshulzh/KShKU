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

package io.github.kshulzh.problemgraph.action

import io.github.kshulzh.problemgraph.context.ProblemContext
import io.github.kshulzh.problemgraph.exception.ActionNotCompletedException

/**
 * Represents a map-based node action capable of handling logic defined by a provided handler function
 * and tracking execution state, result, and related sub-actions.
 *
 * @param T The type of result produced by this node action.
 * @property problemContext Provides the action's execution context, including dispatcher and manager dependencies.
 * @property handler A lambda function defining the action's logic, executed when the action is invoked.
 */
class NodeActionMap<T>(
    val problemContext: ProblemContext,
    val handler: NodeActionMap<T>.() -> T
) : NodeAction, Action<T>, ObservableAction {
    /**
     * A mutable map to cache actions associated with specific keys.
     *
     * The map is used to maintain a relationship between keys (of any type)
     * and corresponding actions. Each key in the map represents a unique identifier,
     * while the value represents an `Action` instance associated with that key.
     *
     * This cache is useful for managing and reusing actions within the context
     * of the containing class, such as tracking or accessing actions by their identifiers.
     */
    val cache = mutableMapOf<Any, Action<*>>()
    /**
     * A set containing the sub-actions associated with this action map.
     *
     * This property retrieves the current sub-actions stored within the action map's internal cache.
     * Each sub-action is represented as an `Action` and corresponds to an independent unit of work.
     *
     * Sub-actions play a critical role in determining the overall status of this node action. They
     * are particularly useful in scenarios where the execution of this action depends on the state
     * or completion of its sub-actions.
     *
     * The subnodes are derived from the cache and returned as a `Set`. This ensures that no duplicate
     * actions are present in the result.
     *
     * @return A set of sub-actions currently present in the action map.
     */
    override val subnodes: Set<Action<*>> get() = cache.values.toSet()
    /**
     * Represents the exception that occurred during the execution of the action.
     *
     * This property holds an instance of `Throwable` when an error occurs during
     * the execution of this `NodeActionMap`. It is set within the `exec` method if
     * an exception is thrown during execution. If no error occurs, the value will
     * be `null`.
     *
     * Implementing classes should ensure proper management of this property during
     * the execution lifecycle to reflect the execution status accurately.
     *
     * This property is particularly useful in determining the failure state of the
     * action and is referenced when attempting to retrieve the `result` or when
     * assessing the action's overall state.
     */
    override var throwable: Throwable? = null
    /**
     * Tracks whether the action has been successfully completed.
     *
     * This property is set to `true` if the action has executed without exceptions
     * and its result is available. It is set to `false` if the action has either not started
     * or has failed due to an exception during execution. The value of `completed` is managed
     * by the `exec` function, which updates it based on the execution outcome.
     *
     * This property is commonly used to check the current state of the action
     * and make decisions regarding the readiness of its result or whether retry logic is needed.
     */
    override var completed: Boolean = false
    /**
     * A mutable variable that stores local data or intermediate results
     * relevant to the execution of the node action.
     *
     * The value of this property is set during the execution (`exec()`) of
     * the node action. It holds the result of the handler invocation or
     * may store any data required for processing within the action's lifecycle.
     *
     * It is a nullable type, allowing it to represent the absence of a value
     * before or after execution. Proper casting and null checks should be
     * employed when accessing this property to ensure safe usage.
     */
    var local: Any? = null

    /**
     * Executes the action logic defined within the `handler` function of the `NodeActionMap`.
     *
     * This method is responsible for invoking the `handler` associated with this `NodeActionMap`.
     * Upon invocation, it updates the internal state of the object to reflect the status
     * of execution:
     *
     * - Sets `completed` to `false` before execution starts.
     * - Calls the `handler` function and stores its result in the `local` property.
     * - If the execution succeeds without exceptions, updates `completed` to `true`
     *   and clears the `throwable` property.
     * - If an exception occurs during execution, captures the exception in the `throwable`
     *   property and sets `completed` to `false`.
     *
     * This method is crucial for managing the lifecycle of an action by encapsulating
     * its execution logic and maintaining its internal state based on the result
     * of execution.
     *
     * Implementations should ensure that the method is called according to the expected
     * lifecycle of the `NodeActionMap` to guarantee proper state management.
     */
    override fun exec() {
        try {
            completed = false
            local = handler()
            completed = true
            throwable = null
        } catch (e: Throwable) {
            throwable = e
            completed = false
        }
    }

    /**
     * Returns the result of the action if it has completed successfully, or throws an exception
     * if the action is either not completed or has encountered an error.
     *
     * This property performs the following checks when accessed:
     * - If the `throwable` property is not null, the contained exception is thrown.
     * - If the action has not been completed (`completed` is `false`), an `ActionNotCompletedException` is thrown.
     * - Otherwise, the `local` property is returned as the result, cast to the expected type `T`.
     *
     * Accessing this property is intended for scenarios where the action is expected to have either
     * completed successfully or failed, and is not intended to be used while the action is still running.
     *
     * @throws Throwable If the `Action` failed during execution.
     * @throws ActionNotCompletedException If the `Action` has not yet finished execution.
     */
    override val result: T get() {
        val t = throwable
        if (t != null) {
            throw t
        } else if (completed) {
            @Suppress("UNCHECKED_CAST")
            return local as T
        } else {
            throw ActionNotCompletedException(this)
        }
    }
    /**
     * A mutable list of observers associated with this action.
     *
     * Observers are typically other `Action` instances that are notified or depend on the
     * completion or result of this action. The list allows for dynamic management of dependent
     * actions, including adding or removing observers during runtime.
     *
     * This property is particularly useful for managing dependencies between actions,
     * enabling complex workflows where the behavior of one action is influenced by the state
     * or result of another.
     */
    override val observers: MutableList<Action<*>> = mutableListOf()
}