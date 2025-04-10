package com.github.kshulzh.kshku.signals

inline fun <reified T : Any> signal(noinline body: (T)->Unit) : SignalCatcher{
    return SignalCatcherImpl().also {
        it.addCatcher(T::class, body)
    }
}

suspend inline fun <reified T : Any> signalSuspend(noinline body: suspend (T)->Unit) : CoroutineSignalCatcher{
    return CoroutineSignalCatcherImpl().also {
        it.addCatcher(T::class, body)
    }
}