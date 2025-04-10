package com.github.kshulzh.kshku.local.coroutine

import com.github.kshulzh.kshku.local.thread.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.coroutineContext

interface CoroutineLocalContext<T> {
    suspend fun getValue() : T?
    suspend fun setValue(value: T?)
}