package com.github.kshulzh.kshku.signals

import com.github.kshulzh.kshku.local.coroutine.CoroutineLocalContextHierarchical
import com.github.kshulzh.kshku.local.thread.ThreadLocalContext
import kotlin.reflect.KClass

interface CoroutineSignalCatcher {
    companion object {
        var signal = CoroutineLocalContextHierarchical<CoroutineSignalCatcher>()
    }
    fun <T : Any> addCatcher(kClass: KClass<*>, body: suspend (T)->Unit)
    suspend fun <T> handle(obj: T)
    suspend fun setup();
    suspend fun delete();
}

inline infix fun <reified T : Any> CoroutineSignalCatcher.signal(noinline body: suspend (T)->Unit) : CoroutineSignalCatcher{
    return this.also {
        addCatcher(T::class, body)
    }
}

suspend inline fun <T>CoroutineSignalCatcher.run(body: ()->T) : T {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}

suspend inline fun <T>CoroutineSignalCatcher.runNullable(body: ()->T?) : T? {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}