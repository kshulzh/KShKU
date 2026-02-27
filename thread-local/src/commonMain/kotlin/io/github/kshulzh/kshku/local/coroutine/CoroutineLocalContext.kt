package io.github.kshulzh.kshku.local.coroutine

interface CoroutineLocalContext<T> {
    suspend fun getValue() : T?
    suspend fun setValue(value: T?)
}