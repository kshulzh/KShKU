package io.github.kshulzh.kshku.local.coroutine

import io.github.kshulzh.kshku.local.thread.LocalContext
import io.github.kshulzh.kshku.local.thread.ThreadLocalContext

class CoroutineLocalContextHierarchicalInThread<T>(
    val localContext: LocalContext<T> = ThreadLocalContext()
) : CoroutineLocalContextHierarchical<T>() {
    override suspend fun getValue(): T? {
        return super.getValue() ?: localContext.value
    }
}
