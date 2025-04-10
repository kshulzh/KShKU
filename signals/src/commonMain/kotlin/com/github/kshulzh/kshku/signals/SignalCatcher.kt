package com.github.kshulzh.kshku.signals

import com.github.kshulzh.kshku.local.thread.ThreadLocalContext
import kotlin.reflect.KClass

interface SignalCatcher {
    companion object {
        var signal by ThreadLocalContext<SignalCatcher>()::value
    }
    fun <T : Any> addCatcher(kClass: KClass<*>, body: (T)->Unit)
    fun <T> handle(obj: T)
    fun setup();
    fun delete();
}

inline infix fun <reified T : Any> SignalCatcher.signal(noinline body: (T)->Unit) : SignalCatcher{
    return this.also {
        addCatcher(T::class, body)
    }
}

inline fun <T>SignalCatcher.run(body: ()->T) : T {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}

inline fun <T>SignalCatcher.runNullable(body: ()->T?) : T? {
    setup()
    try {
        return body()
    } finally {
        delete()
    }
}