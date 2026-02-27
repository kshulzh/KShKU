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

package io.github.kshulzh.problemgraph.expect

/**
 * Represents an expectation that combines multiple individual expectations and validates a value against all of them.
 *
 * The `MultiExpect` class serves as a composite implementation of the `Expect` interface. It allows
 * combining multiple expectations, represented as a set of `Expect` instances, into a single entity.
 * When a value is validated, it is checked against each of the individual expectations, and validation
 * stops as soon as an unmet expectation is found.
 *
 * @param T The type of value that this expectation applies to.
 * @property expects A mutable set of `Expect` instances that this composite expectation consists of.
 */
class MultiExpect<T>(
    val expects: MutableSet<Expect<T>> = mutableSetOf()
) : Expect<T> {

    /**
     * Evaluates the given value against all expectations in the `expects` set.
     *
     * This method iterates through all expectations in the `expects` collection and applies
     * their respective `expected` method to the provided value. If any expectation is not satisfied,
     * it returns the corresponding `Throwable`. The first non-null `Throwable` encountered is returned.
     * If all expectations are satisfied, it returns `null`.
     *
     * @param value The value to be evaluated against the expectations in the `expects` set.
     * @return A `Throwable` describing the first unmet expectation, or `null` if all expectations are satisfied.
     */
    override fun expected(value: T): Throwable? {
        return expects.map { it.expected(value) }.firstOrNull { it != null }
    }
}