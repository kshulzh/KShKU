package com.github.kshulzh.kshku.local.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

//
//class CoroutineLocalDelegate<T>(val coroutineContext: CoroutineContext, val coroutineLocalContext: CoroutineLocalContext<T>) {
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
//        return "$thisRef, thank you for delegating '${property.name}' to me!"
//    }
//
//    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        println("$value has been assigned to '${property.name}' in $thisRef.")
//    }
//}
//
//inline fun <T> CoroutineScope.coroutineDelegate(coroutineLocalContext: CoroutineLocalContext<T> = CoroutineLocalContextHierarchical()) = CoroutineLocalDelegate(coroutineContext, coroutineLocalContext)