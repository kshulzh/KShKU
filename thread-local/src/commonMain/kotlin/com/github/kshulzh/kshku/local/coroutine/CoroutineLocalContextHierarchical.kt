package com.github.kshulzh.kshku.local.coroutine

import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlin.coroutines.coroutineContext

open class CoroutineLocalContextHierarchical<T>() : CoroutineLocalContextImpl<T>() {
    override suspend fun getValue(): T? {
        return super.getValue() ?: getValue(coroutineContext.job.parent)
    }
    private fun getValue(job: Job?): T? {
        return if (job == null) null else map[job] ?: getValue(job.parent)
    }
}