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
 * Thrown when attempting to access the result of an `Action` that has not been completed yet.
 *
 * This exception indicates that the `Action` is either still in progress or has failed to execute successfully,
 * making it impossible to retrieve its result. It serves as a specific indicator that the `Action`
 * is not in a completed state.
 *
 * @constructor Creates an `ActionNotCompletedException` for the given `Action`.
 * @param action The `Action` that was accessed before being completed.
 */
class ActionNotCompletedException(
    action: Action<*>
) : ActionException(action)