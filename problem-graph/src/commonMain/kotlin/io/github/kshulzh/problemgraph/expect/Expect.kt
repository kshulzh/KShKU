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
 * Represents an interface defining validation logic for a specific type of value `T`.
 *
 * The `Expect` interface is intended to be implemented by classes that impose certain
 * expectations or constraints on values. Implementations of this interface should provide
 * a mechanism to validate the input value and return an appropriate exception if the value
 * does not meet the expected criteria.
 *
 * @param T The type of value that this expectation applies to.
 */
interface Expect<T> {
    /**
     * Evaluates whether the provided value satisfies the expectations defined by the implementation.
     *
     * This function checks if a given value meets specific expectations. If the value does
     * not satisfy the expectations, a corresponding `Throwable` is returned that describes
     * the failure. If the value is valid and adheres to the expectations, the function
     * returns `null`.
     *
     * @param value The value to be evaluated against the expectations.
     * @return A `Throwable` instance describing why the value does not meet the expectations,
     *         or `null` if the value is valid.
     */
    fun expected(value: T) : Throwable?
}