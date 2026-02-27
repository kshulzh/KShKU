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

package io.github.kshulzh.problemgraph.action

import io.github.kshulzh.problemgraph.context.ProblemContext

/**
 * Represents an extension to a `NodeAction` allowing for the addition of optional sub-actions.
 * These optional sub-actions do not block the primary flow of execution but can be used to
 * execute ancillary tasks or provide additional context.
 *
 * This interface introduces a new layer of granularity, allowing for differentiation between
 * mandatory sub-actions (managed in `NodeAction`) and optional sub-actions. Optional sub-actions
 * are managed using a mutable map structure, enabling dynamic addition, retrieval, and management
 * of actions by associating them with specific keys.
 */
interface OptionalSubActions {
    /**
     * A mutable map holding elements associated with actions that produce no result (`Unit`).
     * The keys are of type `Any`, and the values are actions (`Action<Unit>`).
     *
     * This map is often used to manage a collection of optional actions or tasks
     * that can be executed and tracked within the context of an implementation.
     *
     * Each action in the map is responsible for tracking its own execution state,
     * including completion and potential errors.
     */
    val optional: MutableMap<Any, Action<Unit>>
    /**
     * Represents the context associated with a problem or task, encapsulating information
     * necessary for managing actions and their execution.
     *
     * The `problemContext` provides access to key components, such as the `ActionDispatcher`
     * and `ActionManager`, which are responsible for orchestrating and managing actions
     * within a specific problem domain. It is often used in environments where actions
     * need to be coordinated, executed, or tracked as part of a larger workflow.
     */
    val problemContext: ProblemContext
}