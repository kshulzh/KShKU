package io.github.kshulzh.kshku.local.coroutine

import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

open class CoroutineLocalContextImpl<T> : CoroutineLocalContext<T> {
    val map = mutableMapOf<CoroutineContext, T?>()
    override suspend fun getValue(): T? {
        return map[coroutineContext.job]
    }

    override suspend fun setValue(value: T?) {
        map[coroutineContext.job] = value
    }
}
