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

package io.github.kshulzh.problemgraph.context

/**
 * Represents the context in which problems, actions, and their workflows are handled.
 *
 * The `ProblemContext` class provides a central point for managing and dispatching
 * actions using its encapsulated `ActionDispatcher` and `ActionManager` components.
 * It facilitates the coordination and execution of various actions while maintaining
 * the necessary context and state required for effective problem resolution.
 *
 * @property actionDispatcher The component responsible for dispatching and executing actions.
 *                            It manages the execution lifecycle of actions, including initiating
 *                            and handling their outcomes.
 * @property actionManager The component responsible for managing the state and lifecycle
 *                         of actions. It organizes, tracks, and resolves the actions
 *                         submitted within the system.
 */
class ProblemContext(
    val actionDispatcher: ActionDispatcher,
    val actionManager: ActionManager
)