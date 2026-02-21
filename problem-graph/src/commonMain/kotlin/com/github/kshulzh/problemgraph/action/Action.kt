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
 * Represents a generic action or task that can be executed and produces a result upon completion.
 * The action can track its completion status and any occurring exception.
 *
 * @param T The type of the result produced by the action.
 */
interface Action<T> {
    /**
     * Represents an exception that occurred during the execution of an `Action`.
     *
     * This property holds the throwable instance if the action execution resulted
     * in an error. If no error occurred during execution, it will be `null`.
     */
    val throwable: Throwable?
    /**
     * Indicates whether the action has been successfully completed.
     *
     * The value is `true` if the action has finished execution without throwing any exceptions,
     * otherwise it is `false`. This property can be used to determine the status of an action
     * and assess whether further steps are required or the result is available.
     */
    val completed: Boolean
    /**
     * Executes the logic encapsulated within the Action. The method ensures the execution
     * of the dependent logic defined by the implementing class. If the execution completes
     * successfully, the `completed` property is updated to `true`, and any previously stored
     * throwable is cleared. If an exception occurs during execution,
     * the `throwable` property is updated accordingly and the `completed` property is set to `false`.
     *
     * This method is a crucial part of the Action lifecycle and is typically used
     * by dispatchers, managers, or other orchestrators to trigger the action's behavior.
     * Implementing classes should ensure proper exception handling and updates to
     * the state of the Action.
     */
    fun exec()
    /**
     * Represents the result of an `Action` execution.
     *
     * This property provides the output of the `Action` when it has completed successfully.
     * If the `Action` is still incomplete or has thrown an exception during execution, accessing
     * this property will result in an exception being thrown.
     *
     * When accessed:
     * - If the `Action`'s `throwable` property contains an exception, that exception will be thrown.
     * - If the `Action` has not completed yet (`completed` is `false`), an `ActionNotCompletedException` will be thrown.
     * - Otherwise, the result of the completed `Action` will be returned.
     *
     * Use this property to retrieve the result of the `Action` once it has completed successfully.
     */
    val result : T
}

/**
 * Checks if the action has been finished, either by completing successfully or by encountering an error.
 *
 * An action is considered finished if it has either been completed (`completed == true`)
 * or if it has encountered an exception (`throwable != null`).
 *
 * @return true if the action is finished (either completed successfully or with an error), false otherwise.
 */
fun <T> Action<T>.finished(): Boolean {
    return completed || throwable != null
}