package com.github.kshulzh.kshku.signals

inline fun <reified T : Any> signal(obj: T) {
    SignalCatcher.signal?.handle(obj)
}

suspend inline fun <reified T : Any> signalSuspend(obj: T) {
    CoroutineSignalCatcher.signal.getValue()?.handle(obj)
}



