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

package com.github.kshulzh.problemgraph.expect

import com.github.kshulzh.problemgraph.action.Action

/**
 * Represents an abstract interface that defines an action capable of managing expectations for results.
 *
 * The `ExpectableAction` interface specifies a contract where actions can validate
 * their results against a set of defined expectations, mapping `Action` instances to
 * their respective expectations. If a result does not fulfill the expectations during validation,
 * an exception is returned describing the specific failure.
 *
 * @param T The type of the result that this action handles.
 */
interface ExpectableAction<T> {
    /**
     * A map that holds expectations associated with specific actions.
     *
     * This property maps an `Action` to its corresponding `Expect` instance. The `Expect` instance
     * defines the validation logic or expectations for the result produced by the associated `Action`.
     *
     * Each entry in the map associates an `Action` with an `Expect` that validates the result of the action.
     * The validation is performed by invoking the `expected` method of the `Expect` implementation,
     * which returns a `Throwable` if the result does not meet the expectations or `null` if it is valid.
     *
     * In the context of the `ExpectableAction` interface, this map is used to manage and evaluate
     * expectations for multiple actions and their results. Methods like `expect` retrieve these
     * expectations to validate the outcomes of actions.
     *
     * Key Type:
     * - `Action<*>`: Represents an action or task that can produce results.
     *
     * Value Type:
     * - `Expect<T>`: Represents the validation logic to evaluate the result of the associated action.
     */
    val expects: MutableMap<Action<*>, Expect<T>>

    /**
     * Evaluates the provided result against a set of expectations defined in the `expects` map.
     *
     * This method iterates through the collection of expectations, applying each expectation's
     * validation logic to the provided result. If any expectation is not satisfied, it returns
     * the associated `Throwable` describing the failure. If all expectations are satisfied, it
     * returns `null`.
     *
     * @param res The result to be evaluated against the set of expectations.
     * @return A `Throwable` describing the first unmet expectation, or `null` if all expectations are satisfied.
     */
    fun expect(res: T) : Throwable? {
        return expects.values.firstNotNullOfOrNull { it.expected(res) }
    }
}