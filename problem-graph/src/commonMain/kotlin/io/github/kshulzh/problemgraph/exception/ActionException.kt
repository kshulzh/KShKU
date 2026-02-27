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

package io.github.kshulzh.problemgraph.exception

import io.github.kshulzh.problemgraph.action.Action

/**
 * Represents an exception that occurs during the execution of a specific action.
 *
 * The `ActionException` class is used to encapsulate errors or exceptional situations
 * related to the processing or execution of an `Action`. It serves as the base class for
 * more specific exceptions tied to actions and provides access to the associated action
 * instance that caused the exception.
 *
 * @property action The action instance that encountered an error or caused the exception.
 * The `action` property provides context about the specific action associated with this exception,
 * allowing for detailed inspection or handling.
 */
open class ActionException (
    val action: Action<*>,
) : RuntimeException()