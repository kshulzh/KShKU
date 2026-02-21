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

/**
 * Represents an expectation that accepts any value without applying constraints or conditions.
 *
 * The `AnythingExpect` class is a generic implementation of the `Expect` interface. It always
 * resolves as valid for any given input value and does not produce any exceptions, effectively
 * serving as a permissive or no-op expectation.
 *
 * @param T The type of value this expectation applies to.
 */
class AnythingExpect<T> : Expect<T> {
    /**
     * Evaluates whether the provided value satisfies the expectations defined by the implementation.
     * If the value does not meet the expectations, a corresponding `Throwable` is returned,
     * otherwise `null` is returned to indicate that the value is acceptable.
     *
     * @param value The value to be evaluated against the expectations.
     * @return A `Throwable` instance describing why the value does not meet the expectations,
     *         or `null` if the value is valid.
     */
    override fun expected(value: T): Throwable? = null
}