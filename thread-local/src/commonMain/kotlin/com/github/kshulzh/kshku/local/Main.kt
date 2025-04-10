package com.github.kshulzh.kshku.local

import com.github.kshulzh.kshku.local.coroutine.CoroutineLocalContextHierarchical
import com.github.kshulzh.kshku.local.coroutine.CoroutineLocalContextHierarchicalInThread
import com.github.kshulzh.kshku.local.thread.ThreadLocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

val global = CoroutineLocalContextHierarchical<String>()

fun main() {
    val local by ThreadLocalContext<String>()::value
    Delegates
    runBlocking {
        global.setValue("Hello world")
        launch {
            global.setValue("Good bye")
            println(global.getValue())
            global.setValue(null)
            launch {
                println(global.getValue())
            }
        }
        println(global.getValue())
    }
}