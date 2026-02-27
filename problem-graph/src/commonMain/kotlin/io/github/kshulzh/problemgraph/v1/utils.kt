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

package io.github.kshulzh.problemgraph.v1

import io.github.kshulzh.problemgraph.action.Action
import io.github.kshulzh.problemgraph.action.NodeActionImpl
import io.github.kshulzh.problemgraph.action.ObservableAction
import io.github.kshulzh.problemgraph.action.finished
import io.github.kshulzh.problemgraph.action.NodeActionMap
import io.github.kshulzh.problemgraph.action.OptionalSubActions
import io.github.kshulzh.problemgraph.context.ActionExecutor
import io.github.kshulzh.problemgraph.context.ProblemContext
import io.github.kshulzh.problemgraph.context.dispatchIfNotFinished
import io.github.kshulzh.problemgraph.exception.ActionException
import io.github.kshulzh.problemgraph.expect.AnythingExpect
import io.github.kshulzh.problemgraph.expect.Expect
import io.github.kshulzh.problemgraph.expect.ExpectableAction
import io.github.kshulzh.problemgraph.expect.MultiExpect
import kotlin.reflect.KProperty


/**
 * Tries to retrieve an existing action from the map or creates a new one using the provided action factory.
 *
 * @param expects A set of expectations that the action is required to satisfy. Defaults to an empty set if not specified.
 * @param actionFactory A lambda function that provides the action to be created if it does not already exist in the map.
 * @return The retrieved or newly created action of type `Action<T>`.
 */
fun <T : Any, R> NodeActionMap<R>.tryGet(expects: Set<Expect<T>> = emptySet(), actionFactory: () -> Action<T>): Action<T> {
    val action = actionFactory()
    // preserve previous behavior that used actionFactory.toString() as a key
    return this.tryGet(actionFactory.toString(), action, expects)
}

/**
 * Attempts to retrieve an existing action associated with the given key from the cache.
 * If no action exists for the key, the provided action is added to the cache, linked with its expectations, and returned.
 * If an existing action is found but differs from the provided action, the existing action is unlinked,
 * the new action is linked and added to the cache, and the new action is returned.
 *
 * @param key The key used to identify the action in the cache.
 * @param action The action to use or cache if none exists for the given key.
 * @param expects An optional set of expectations to link with the action; defaults to an empty set.
 * @return The action associated with the provided key, either from the cache or the newly linked action.
 */
fun <T : Any, R> NodeActionMap<R>.tryGet(key: Any, action: Action<T>, expects: Set<Expect<T>> = emptySet()): Action<T> {
    val expectation = constructExpectation(expects)

    val existing = cache[key]
    if (existing == null) {
        cache[key] = action
        this.link(action, expectation)
        if (!finished()) problemContext.actionDispatcher.dispatch(action)
        return action
    }

    if (action == existing) return action

    this.unlink(existing)
    this.link(action, expectation)
    cache[key] = action
    return action
}

/**
 * Attempts to retrieve the given action, linking it if it is not already a part of the subnodes.
 * This method ensures proper dispatching and tracking of the action.
 *
 * @param T The type of the action.
 * @param R The result type associated with the `NodeActionImpl`.
 * @param action The action to be retrieved or linked.
 * @param expects A set of expectations for the action. Defaults to an empty set.
 * @return The provided action.
 */
fun <T : Any, R> NodeActionImpl<R>.tryGet(action: Action<T>, expects: Set<Expect<T>> = emptySet()): Action<T> {
    val expectation = constructExpectation(expects)
    val anyAction = action as Action<*>

    if (anyAction !in subnodes) {
        link(action, expectation)
        subnodes.add(anyAction)
    }

    usedSubnodes.add(anyAction)
    problemContext.actionDispatcher.dispatchIfNotFinished(action)
    return action
}

/**
 * Links an `Action` to another `Action` with an optional validation expectation.
 *
 * This function establishes a relationship between a source `Action` (the current instance)
 * and a target `Action`. If the target `Action` is an `ObservableAction`, the source
 * `Action` will be added to the list of observers of the target. If the target `Action`
 * is an `ExpectableAction`, the provided expectation will be associated with the source
 * `Action` in the `expects` map of the target.
 *
 * @param action The target `Action` to be linked with the current `Action`.
 * @param expect The optional `Expect<R>` used to validate the target `Action`'s result.
 *               Defaults to an instance of `AnythingExpect`, allowing all results without validation.
 * @param T The type of the result produced by the current `Action`.
 * @param R The type of the result produced by the target `Action`.
 */
fun <T, R> Action<T>.link(action: Action<R>, expect: Expect<R> = AnythingExpect()) {
    if (action is ObservableAction && this !in action.observers) {
        action.observers.add(this)
    }
    if (action is ExpectableAction<*>) {
        @Suppress("UNCHECKED_CAST")
        val local = action as ExpectableAction<R>
        local.expects[this] = expect
    }
}

/**
 * Unlinks the current action from the specified action by removing it from the observers
 * or expectations of the target action. This method is used to break the association
 * between two actions, ensuring that the current action is no longer treated as a dependent
 * or an observer of the target action.
 *
 * @param action The action from which the current action will be unlinked. If the specified
 * action implements `ObservableAction`, the current action is removed from its observers list.
 * If the action implements `ExpectableAction`, the current action is removed from its expectations.
 */
fun <T, R> Action<T>.unlink(action: Action<R>) {
    if (action is ObservableAction) {
        action.observers.remove(this)
    }
    if (action is ExpectableAction<*>) {
        action.expects.remove(this)
    }
}

/**
 * Constructs a composite expectation or selects an existing expectation based on the provided set of expectations.
 *
 * This function determines the resulting expectation as follows:
 * - If the provided set is empty, an `AnythingExpect` instance is returned.
 * - If the set contains a single expectation, that expectation is returned.
 * - If the set contains multiple expectations, a `MultiExpect` instance is created containing them.
 *
 * @param T the type of value that the expectations apply to.
 * @param expects a set of `Expect` instances that define the desired validation logic. Defaults to an empty set.
 * @return an `Expect` instance representing the composite or individual expectation(s).
 */
fun <T> constructExpectation(expects: Set<Expect<T>> = emptySet()): Expect<T> {
    return when {
        expects.isEmpty() -> AnythingExpect()
        expects.size == 1 -> expects.first()
        else -> MultiExpect(expects.toMutableSet())
    }
}


/**
 * Retrieves the value of an `Action` when accessed through a delegate property.
 *
 * This method evaluates whether the `Action` has completed or encountered an exception
 * and returns the result appropriately. If the action has completed successfully,
 * the result is returned. If an exception occurred during execution, the exception is re-thrown.
 * If the `Action` has not yet completed, its `exec` method is called recursively
 * until completion or failure.
 *
 * @param thisRef The reference to the object that owns the property being delegated.
 * @param property The metadata for the property being delegated.
 * @return The result of the `Action` upon successful completion.
 * @throws ActionException If an exception occurred during the execution of the `Action`.
 */
operator fun <T> Action<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    val throwable = throwable
    if (throwable!=null) {
        throw ActionException(this)
    } else {
        if (completed) {
            return result
        } else {
            exec()
            return getValue(thisRef, property)
        }
    }
}
/**
 * Provides a delegate transformation to extract a list of results from a list of actions
 * when accessed via Kotlin property delegation syntax.
 *
 * @param thisRef The object for which the property is being accessed, or null if not applicable.
 * @param property The metadata for the property being delegated.
 * @return A list of results extracted from the provided list of actions.
 */
operator fun <T> List<Action<T>>.getValue(thisRef: Any?, property: KProperty<*>): List<T> {
    return getValue()
}
/**
 * Extracts the result of each `Action` in the list and returns them as a new list.
 *
 * This function maps over the list of `Action` objects, accessing the `result` property of each
 * action and collecting the results into a new list.
 *
 * @return A list containing the results of each `Action` in the original list.
 */
fun <T> List<Action<T>>.getValue(): List<T> {
    return map { it.result }
}
/**
 * Evaluates and retrieves an instance of type `T` based on the provided expectations and action.
 * This method executes an `Action` while considering a set of `Expect` validations.
 *
 * @param expects A set of expectations (`Expect<T>`) that the result of the action should satisfy. Defaults to an empty set.
 * @param actionGetter A lambda function that provides the action (`Action<T>`).
 * @return The result of the action of type `T` after validation against the expectations.
 */
fun <T : Any, R> NodeActionMap<R>.calc(expects: Set<Expect<T>> = setOf(), actionGetter: ()->Action<T>): T {
    return this.calc(actionGetter.toString(),actionGetter(), expects)
}
/**
 * Computes the result of a given action, managing its lifecycle within the action map cache.
 * The method reuses cached actions if they exist for the specified key; otherwise, it links
 * the new action with specified expectations. If an existing action is replaced, it is unlinked
 * before linking the new action.
 *
 * @param key The unique identifier for the action, used to manage its cache entry.
 * @param action The action to be executed, which contains the logic to compute the result.
 * @param expects A set of expectations to be associated with the action, defaults to an empty set.
 * @return The computed result of the action.
 */
fun <T : Any, R> NodeActionMap<R>.calc(key:Any, action: Action<T>, expects: Set<Expect<T>> = setOf()): T {
    val old = cache[key] ?: run {
        cache[key] = action
        this.link(action, constructExpectation(expects))
        if (!finished()) problemContext.actionDispatcher.dispatch(action)
        return action.result
    }
    if (action == old) {
        return action.result
    }
    this.unlink(old)
    this.link(action, constructExpectation(expects))
    cache[key] = action
    return action.result
}
/**
 * Calculates and retrieves the result of executing an action with optional expectations.
 *
 * @param T The type of the result produced by the action.
 * @param R The generic type related to the `NodeActionImpl`.
 * @param action The action to be executed.
 * @param expects A set of optional expectations for the action, defaulting to an empty set if none are provided.
 * @return The result of the executed action.
 */
fun <T : Any, R> NodeActionImpl<R>.calc(action: Action<T>, expects: Set<Expect<T>> = setOf()): T {
    return get(action, expects).result
}
/**
 * Calculates and retrieves a list of results based on the provided actions and expectations.
 *
 * @param T The type of objects expected as results from the actions.
 * @param R The type of the action handler's result in the NodeActionImpl context.
 * @param actions A list of actions of type `Action<T>` that will be processed.
 * @param expects An optional set of expectations of type `Expect<T>` to validate the results, defaulting to an empty set.
 * @return A list of results of type `T` obtained from the provided actions and validated against expectations.
 */
fun <T : Any, R> NodeActionImpl<R>.calc(actions: List<Action<T>>, expects: Set<Expect<T>> = setOf()): List<T> {
    return get(actions, expects).getValue()
}
/**
 * Processes a list of actions and maps each action using the `NodeActionImpl.get` method,
 * optionally considering a set of expectations.
 *
 * @param T The type parameter representing the type of data within the provided actions.
 * @param R The type parameter representing the result type of the `NodeActionImpl`.
 * @param actions The list of actions to be processed.
 * @param expects An optional set of expected results to be used during processing. Defaults to an empty set.
 * @return A list of processed actions resulting from applying the `NodeActionImpl.get` method.
 */
fun <T : Any, R> NodeActionImpl<R>.get(actions: List<Action<T>>, expects: Set<Expect<T>> = setOf()): List<Action<T>> {
    return actions.map { this.get(it, expects) }
}
/**
 * Retrieves or links an action to the node and updates its expectation set if necessary.
 *
 * @param T the type parameter for the action.
 * @param R the return type parameter for the NodeActionImpl.
 * @param action the action to be retrieved or linked.
 * @param expects a set of expectations to be associated with the action, defaulting to an empty set.
 * @return the action after it has been retrieved or linked with the node.
 */
fun <T : Any, R> NodeActionImpl<R>.get(action: Action<T>, expects: Set<Expect<T>> = setOf()): Action<T> {
    val expect = constructExpectation(expects)
    if (!subnodes.contains(action as Action<*>)) {
        link(action, expect)
        subnodes.add(action)
    }
    if (expects.isNotEmpty() && action is ExpectableAction<*>) {
        @Suppress("UNCHECKED_CAST")
        val local = action as ExpectableAction<T>
        local.expects[this] = expect
        if (action.completed && expect.expected(action.result) !=null) {
            problemContext.actionDispatcher.dispatch(action)
        }
    }
    usedSubnodes.add(action)
    problemContext.actionDispatcher.dispatchIfNotFinished(action)
    return action
}
/**
 * Adds an optional sub-action to the `OptionalSubActions` interface.
 * This method allows the addition of an optional sub-action that is associated
 * with a key derived from the action's class type.
 * The sub-action can provide additional functionality or context within the overall
 * execution flow without blocking or interrupting primary task execution.
 *
 * @param action The lambda function defining the behavior of the optional sub-action.
 *               It is represented as an extension function on `NodeActionImpl<Unit>`.
 */
fun OptionalSubActions.optional(action: NodeActionImpl<Unit>.()->Unit) {
    optional(action::class,action)
}
/**
 * Adds an optional sub-action associated with a specific key and executes it if not already present.
 *
 * This method checks if the provided `key` is already present in the `optional` map.
 * If it is not present, it creates a new `NodeActionImpl`, associates it with the key,
 * and dispatches the action using the `problemContext`'s action dispatcher.
 *
 * @param key The identifier for the optional action to be added or checked.
 * @param action A lambda defining the behavior of the optional action, executed in the context of `NodeActionImpl`.
 */
fun OptionalSubActions.optional(key:Any, action:NodeActionImpl<Unit>.()->Unit) {
    if (key !in optional) {
        val a = NodeActionImpl(problemContext, action)
        optional[key] = a
        problemContext.actionDispatcher.dispatchIfNotFinished(a)
    }
}

fun createProblemContext() : ProblemContext {
    val actionExecutor = ActionExecutor()
    return ProblemContext(actionExecutor, actionExecutor)
}


