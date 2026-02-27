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

/**
 * Represents an action performed on a node, which may have sub-actions and is capable
 * of determining whether it is blocked based on the status of its sub-actions.
 *
 * A `NodeAction` is composed of a set of sub-actions, where each sub-action
 * must represent an implementation of the `Action` interface. Sub-actions are
 * considered when determining the overall status of the node.
 */
interface NodeAction {
    /**
     * A set representing the sub-actions or sub-tasks associated with this node.
     * Each sub-action is an instance of `Action` and can independently track its own execution status.
     *
     * Sub-actions play a crucial role in determining the state of the parent node. For instance,
     * they can be used to ensure that all dependent actions are completed before proceeding
     * with further logic.
     *
     * This property is often utilized for operations such as checking whether all sub-actions
     * are completed (`isNotBlocked`) or for managing dependencies in complex workflows.
     */
    val subnodes: Set<Action<*>>
    /**
     * Checks whether the current node is not blocked, based on the completion status
     * of all its associated subnodes.
     *
     * @return true if all subnodes of the current node are completed, false otherwise.
     */
    fun isNotBlocked(): Boolean = subnodes.all { it.completed }
}