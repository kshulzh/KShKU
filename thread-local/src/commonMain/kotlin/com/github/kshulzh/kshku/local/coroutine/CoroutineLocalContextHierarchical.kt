package com.github.kshulzh.kshku.local.coroutine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlin.coroutines.coroutineContext

open class CoroutineLocalContextHierarchical<T>() : CoroutineLocalContextImpl<T>() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getValue(): T? {
        return super.getValue() ?: getValue(coroutineContext.job.parent)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getValue(job: Job?): T? {
        return if (job == null) null else map[job] ?: getValue(job.parent)
    }
}