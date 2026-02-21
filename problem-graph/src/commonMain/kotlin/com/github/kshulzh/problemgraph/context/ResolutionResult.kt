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

/**
 * Encapsulates the result of the resolution process within an action management system.
 *
 * This class is used to represent the outcome of resolving actions managed by an `ActionManager`.
 * It contains two main properties that provide details on the current state of actions and
 * their execution queue.
 *
 * @property actions A list of actions that are considered as resolved or are part of the resulting state.
 * These actions typically represent tasks that have been processed or are identified for resolution.
 *
 * @property queue A list of actions representing the current execution queue.
 * These actions are awaiting execution, in progress, or queued for further processing.
 */
data class ResolutionResult(
    val actions: List<Action<*>> = emptyList(),
    val queue: List<Action<*>> = emptyList()
)