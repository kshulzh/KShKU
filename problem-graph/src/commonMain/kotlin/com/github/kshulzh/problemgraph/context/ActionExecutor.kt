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
import com.github.kshulzh.problemgraph.action.NodeAction
import com.github.kshulzh.problemgraph.action.ObservableAction
import com.github.kshulzh.problemgraph.action.finished
import com.github.kshulzh.problemgraph.exception.ActionException

/**
 * An implementation of the `ActionDispatcher` and `ActionManager` interfaces, responsible
 * for managing and resolving a queue of actions. This class ensures actions are executed,
 * handles their lifecycle, manages progress, and processes dependencies or errors.
 *
 * @param errorHandler A function to handle errors occurring during action dispatching.
 *                     It accepts an `Action` and a `Throwable` instance, returning `true` if the error
 *                     can be handled, otherwise `false`. Defaults to an error handler that always returns `false`.
 */
class ActionExecutor(
    val errorHandler: (Action<*>, Throwable) -> Boolean = { _, _ -> false }
) : ActionDispatcher, ActionManager {

    /**
     * Represents a collection used to manage the current set of actions pending execution.
     * This mutable set maintains the order of insertion while ensuring that each action is
     * only present once within the set. It is used during the dispatching, resolution, and
     * notification processes to handle actions that need to be processed or revisited.
     *
     * This property is central to the action dispatching mechanism. Actions are added to the queue
     * when submitted for execution, during resolution cycles, or upon observer notification,
     * and are then processed and dispatched for execution in sequence.
     */
    private var queue: MutableSet<Action<*>> = linkedSetOf()
    /**
     * Tracks whether progress has been made during the resolution or dispatching process.
     *
     * This variable is used to indicate if new actions have been added to the processing queue
     * or if a significant change has occurred in the execution state. It plays a critical role
     * in controlling iterative processes, ensuring they continue only as long as progress is made.
     *
     * The default value is `false`, signaling no progress. It may be updated to `true` during
     * the execution of various methods, such as `resolve`, `handleNotFinished`, or `handleFinishedWithError`,
     * when an action is successfully completed, errors are handled, or dependent tasks advance.
     */
    private var progressMade: Boolean = false

    /**
     * Submits the specified action for execution by adding it to the queue.
     *
     * The action represents a task or operation to be executed. By submitting it,
     * the action will be managed and processed according to the underlying dispatcher's
     * logic, such as execution, retrying, or notifying observers.
     *
     * @param action The action to be submitted, encapsulating the logic to be executed and its state.
     */
    override fun submit(action: Action<*>) {
        queue.add(action)
    }

    /**
     * Resolves the current queue of actions by processing actions iteratively until no new actions are enqueued,
     * ensuring all actions are either completed or appropriately handled.
     *
     * This method initiates processing with the current queue of actions, dispatching each action within the queue.
     * While processing, if new actions are added to the queue (indicating dependencies or further tasks), the method
     * continues running until no new actions appear and no additional progress has been made.
     *
     * @return A [ResolutionResult] containing the list of initially processed actions and the remaining actions in the queue
     *         that were not fully resolved during the process.
     */
    override fun resolve(): ResolutionResult {
        // initial batch to process
        progressMade = true
        var actions = queue
        val initial = actions.toList()
        queue = linkedSetOf()
        actions.forEach { dispatch(it) }

        // continue processing while new actions appear and there was progress
        do {
            progressMade = false
            actions = queue
            queue = linkedSetOf()
            actions.forEach { dispatch(it) }
        } while (queue.isNotEmpty() && progressMade)

        return ResolutionResult(initial, queue.toList())
    }

    /**
     * Dispatches the given action for processing based on its current state.
     *
     * This method evaluates the state of the provided action and handles it accordingly:
     * - If the action has not finished, it invokes `handleNotFinished` to execute the action.
     * - If the action has finished but not successfully completed, it invokes `handleFinishedWithError`
     *   to attempt recovery or handle errors.
     * - If the action has successfully completed, it invokes `handleCompleted` to update its state
     *   and notify observers if necessary.
     *
     * @param action The action to be dispatched. Its execution state will determine how it is processed.
     */
    override fun dispatch(action: Action<*>) {
        when {
            !action.finished() -> handleNotFinished(action)
            action.finished() && !action.completed -> handleFinishedWithError(action)
            else -> handleCompleted(action)
        }
    }

    /**
     * Handles the execution of an action that has not yet finished.
     *
     * The method executes the provided action, updates the progress state if the action
     * completes successfully, and performs any necessary post-processing by delegating
     * to the `onFinished` method.
     *
     * @param action The action to evaluate and process. This action is executed if it has
     *               not finished, and further actions may be triggered based on its result
     *               or completion state.
     */
    private fun handleNotFinished(action: Action<*>) {
        action.exec()
        if (action.completed) {
            progressMade = true
        }
        onFinished(action)
    }

    /**
     * Handles the scenario where an action has finished execution with an error state.
     * This method attempts to recover the action or appropriately handle the error.
     * If recovery is possible, observers are notified. If not, the action is requeued
     * or escalated depending on the nature of the error.
     *
     * @param action The action that has finished with an error. It encapsulates the state,
     *               the result of execution, and any associated exceptions.
     */
    private fun handleFinishedWithError(action: Action<*>) {
        val previous = action.throwable
        action.exec()
        if (action.throwable == null) {
            // recovered from error -> notify dependent observers
            progressMade = true
            notifyObservers(action)
        } else {
            // still failing: maybe the error handler can recover; if not, requeue
            if (previous != null && errorHandler(action, previous)) {
                progressMade = true
            } else if (action.throwable is ActionException) {
                notifyAllObservers(action)
            }
            submit(action)
        }
    }

    /**
     * Handles the completion of the provided action. This function re-executes the given action
     * and compares the current result with the previously stored result. If the results differ,
     * the observers of the action are notified.
     *
     * @param action The action to be processed. This action is executed, and its result is compared
     *               to the previously stored result. If the results differ, observers are notified.
     */
    private fun handleCompleted(action: Action<*>) {
        val prevResult = action.result
        action.exec()
        if (prevResult != action.result) {
            notifyObservers(action)
        }
    }

    /**
     * Handles actions that have reached their finished state by evaluating their throwable status.
     *
     * If the provided action contains a throwable that is not of type `ActionException`, it will be submitted
     * for further processing. This method serves as a step in managing the lifecycle of actions, specifically
     * when an action has encountered an exception during its execution that requires further resolution.
     *
     * @param action The action to be processed. The action's throwable property is used to determine if it needs
     *               further handling or submission.
     */
    private fun onFinished(action: Action<*>) {
        val throwable = action.throwable
        if (throwable != null && throwable !is ActionException) {
            submit(action)
        }
    }

    /**
     * Notifies the observers of the given action that are ready to be processed.
     *
     * If the provided action is an instance of `ObservableAction`, it filters its observers
     * and identifies those that are ready to proceed based on the `isReadyObserver` function.
     * These ready observers are then added to the internal queue for further processing.
     *
     * @param action The action whose observers need to be checked and potentially added to the queue.
     *               This can be any instance of `Action`, but the observers are considered only
     *               if the action is an `ObservableAction`.
     */
    private fun notifyObservers(action: Action<*>) {
        if (action is ObservableAction) {
            val ready = action.observers.filter { isReadyObserver(it) }
            queue.addAll(ready)
        }
    }

    /**
     * Notifies all observers of the given action if the action is an instance of `ObservableAction`.
     *
     * This method checks if the provided action is an `ObservableAction`. If true, it filters the
     * associated observers to include only those that are finished (either completed successfully
     * or with an error), and adds them to the internal queue for further processing.
     *
     * @param action The action whose observers should be notified. This action may encapsulate
     *               a specific task or operation and, if observable, allows its observers
     *               to track its lifecycle or state.
     */
    private fun notifyAllObservers(action: Action<*>) {
        if (action is ObservableAction) {
            queue.addAll(action.observers.filter { it.finished() })
        }
    }

    /**
     * Determines if the given observer is ready for further processing.
     * An observer is considered ready if it has finished its execution and meets the following conditions:
     * - If the observer is an instance of `NodeAction`, it must not be blocked.
     * - If the observer is not an instance of `NodeAction`, the condition is always satisfied.
     *
     * @param observer The observer of type `Action` to be checked for readiness.
     *                 The observer can be either a generic `Action` or a more specific `NodeAction`.
     * @return `true` if the observer is ready for further processing, `false` otherwise.
     */
    private fun isReadyObserver(observer: Action<*>): Boolean {
        return observer.finished() && (observer is NodeAction && observer.isNotBlocked() || observer !is NodeAction)
    }
}
