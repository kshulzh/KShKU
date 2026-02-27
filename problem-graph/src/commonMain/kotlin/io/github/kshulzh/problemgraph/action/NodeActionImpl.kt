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
import io.github.kshulzh.problemgraph.expect.Expect
import io.github.kshulzh.problemgraph.expect.ExpectableAction

/**
 * Implementation of the `NodeAction` interface, representing an action performed on a node
 * with sub-action capabilities and result handling. This class is responsible for executing
 * operations using a provided handler and managing observational, expectation, and optional sub-actions.
 *
 * @param T The type of the result produced by this action.
 * @property problemContext Provides the context required to manage actions like dispatching or handling.
 * @property handler Defines the logic to be executed for the current action.
 */
class NodeActionImpl<T>(
    override val problemContext: ProblemContext,
    val handler: NodeActionImpl<T>.() -> T
) : NodeAction, Action<T>, ObservableAction, ExpectableAction<T>, OptionalSubActions {
    /**
     * A mutable list that holds observers of type `Action`. Observers are actions
     * that are notified or updated in response to changes or events.
     *
     * This list is used to track and manage actions that may observe or depend
     * on the current action or execution context. Observers can be added, removed,
     * or iterated over as part of managing dependent actions or implementing
     * reactive workflows.
     */
    override val observers: MutableList<Action<*>> = mutableListOf()
    /**
     * Stores the mapping of actions to their respective expected results.
     *
     * Each entry in the map associates an `Action` with an instance of `Expect`, which is responsible for
     * handling the expected behavior or output of the action. This property allows for managing and validating
     * expectations related to the actions executed within this context.
     *
     * The mutable nature of the map enables dynamically adding, updating, or removing expectations for actions
     * during the lifecycle of the containing object.
     */
    override val expects: MutableMap<Action<*>, Expect<T>> = mutableMapOf()
    /**
     * A mutable map that associates arbitrary keys with `Action<Unit>` instances.
     *
     * This property is intended for managing optional sub-actions or tasks that are not necessarily
     * part of the core execution logic but may be executed depending on the application's state or requirements.
     * The keys can be of any type and are used to uniquely identify each optional action.
     *
     * An optional action can represent additional behavior or tasks that the system may handle selectively,
     * based on the provided keys or other conditions. These actions do not impact the completion status
     * of the main action unless explicitly invoked.
     */
    override val optional: MutableMap<Any, Action<Unit>> = mutableMapOf()
    /**
     * A mutable set that represents the sub-actions or sub-tasks associated with this node.
     *
     * Each element in this set is an instance of `Action`, allowing for independent tracking
     * of execution status for each sub-action. This property plays a crucial role in maintaining
     * the hierarchical structure of actions, enabling complex workflows by managing dependencies
     * between actions.
     *
     * The set can be modified to dynamically add or remove sub-actions during the execution of
     * the parent action. This flexibility supports a wide variety of use cases, such as dynamically
     * assigning tasks or managing nested logic based on runtime conditions.
     */
    override val subnodes: MutableSet<Action<*>> = mutableSetOf()
    /**
     * Represents the set of sub-actions used during the execution of a node action.
     *
     * This mutable set tracks the `Action` instances that have been utilized or
     * interacted with during the execution of the `NodeActionImpl`. These sub-actions
     * are a subset of the `subnodes` property, providing a specific view on the associated
     * actions that were actively involved in the execution process.
     *
     * The `usedSubnodes` set is updated dynamically during the execution of a node action,
     * ensuring that only the relevant sub-actions are included. It helps in isolating and
     * managing the dependencies or interactions that occurred during the execution lifecycle.
     */
    var usedSubnodes: MutableSet<Action<*>> = mutableSetOf()
    /**
     * Represents an exception that occurred during the execution of this action.
     *
     * This property holds the instance of `Throwable` if the execution of the action
     * resulted in an error. If no error occurred during execution, it will be `null`.
     *
     * The value of this property is updated during the execution lifecycle:
     * - Set to `null` if the execution completes successfully without exceptions.
     * - Populated with the encountered exception if an error occurs during execution.
     *
     * This property is used to track the error state of the action and can be directly
     * queried to determine if an exception was thrown. It may also assist in troubleshooting
     * and debugging when capturing errors from the execution process.
     */
    override var throwable: Throwable? = null
    /**
     * Indicates whether the action has been successfully completed.
     *
     * This property represents the completion state of the action. If the action has executed successfully
     * without throwing exceptions, this property will be set to `true`. Otherwise, it will be `false`,
     * either due to incomplete execution or due to an exception being thrown during its execution.
     *
     * The property is updated within the `exec` method:
     * - It is initially set to `false` at the start of execution.
     * - If the execution succeeds, it is updated to `true`.
     * - If an exception occurs, it remains `false`.
     *
     * This property is used to track the execution status of the action and assess if the associated task
     * has been completed.
     */
    override var completed: Boolean = false
    /**
     * Holds the locally computed result of the action execution.
     *
     * This property is used to store the value resulting from the execution of the
     * `handler` function within the `exec` method. It acts as a temporary container
     * for the result and may subsequently be accessed via the `result` property
     * if the action completes successfully.
     *
     * The value of this property is mutable and its type is generic, allowing for
     * flexibility in handling various types of outcomes from the `handler`.
     *
     * A `null` value indicates either that the action has not been executed yet
     * or that the execution failed without producing a valid result.
     */
    private var local: Any? = null
    /**
     * Executes the action logic defined in the `handler` function and updates the action's state
     * accordingly.
     *
     * This method manages the execution of the action and ensures proper state transitions and
     * exception handling:
     * - Resets the set of used subnodes and marks the action as incomplete before execution.
     * - Calls the `handler` function, expecting it to produce a value that is evaluated via the
     *   associated `expect` method.
     * - Marks the action as completed if the execution finishes without exceptions.
     * - If an exception occurs during the execution of the `handler`, updates the `throwable` property
     *   with the caught exception and marks the action as incomplete.
     *
     * The state updates include:
     * - Clearing or setting the `throwable` property based on the outcome of the `handler` execution.
     * - Updating the `completed` flag to indicate whether the action completed successfully.
     *
     * Calling this method is central to the action's lifecycle, as it ensures the action's logic
     * is executed, and subsequent state-dependent behavior is managed.
     *
     * This method is overridden to implement specific logic tied to the `NodeActionImpl` behavior, such as:
     * - Initializing `usedSubnodes` specific to the node action.
     * - Invoking the `handler` closure and evaluating the returned value against expected conditions.
     */
    override fun exec() {
        try {
            usedSubnodes = mutableSetOf()
            completed = false
            local = handler().also { value ->
                expect(value)
            }
            completed = true
            throwable = null
        } catch (e: Throwable) {
            throwable = e
            completed = false
        }
    }

    /**
     * Retrieves the result of the `NodeActionImpl` execution.
     *
     * - If the action's `throwable` property is not null, the corresponding exception is thrown.
     * - If the action has not completed (`completed` is false), an `ActionNotCompletedException` is thrown.
     * - Otherwise, the stored result of the action is returned.
     *
     * This property is used to access the result of an action only after its execution completes successfully.
     * Attempting to access it before completion or in case of an error will result in an exception being thrown.
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
}