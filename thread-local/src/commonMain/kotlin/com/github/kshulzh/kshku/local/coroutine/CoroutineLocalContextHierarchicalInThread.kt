package com.github.kshulzh.kshku.local.coroutine

import com.github.kshulzh.kshku.local.thread.LocalContext
import com.github.kshulzh.kshku.local.thread.ThreadLocalContext

class CoroutineLocalContextHierarchicalInThread<T>(
    val localContext: LocalContext<T> = ThreadLocalContext()
) : CoroutineLocalContextHierarchical<T>() {
    override suspend fun getValue(): T? {
        return super.getValue() ?: localContext.value
    }
}
