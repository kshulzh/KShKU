package com.github.kshulzh.kshku.signals

import kotlin.reflect.KClass

class CoroutineSignalCatcherImpl : CoroutineSignalCatcher {
    var parent: CoroutineSignalCatcher? = null
    val map:MutableMap<KClass<*>, suspend (Any) -> Unit> = linkedMapOf()
    override fun <T : Any> addCatcher(kClass: KClass<*>, body: suspend (T) -> Unit) {
        map[kClass] = body as suspend (Any) -> Unit
    }

    override suspend fun <T> handle(obj: T) {
        map.entries.find {
            it.key.isInstance(obj)
        }?.value?.let { it(obj as Any) } ?: parent?.handle(obj)
    }

    override suspend fun setup() {
        parent = CoroutineSignalCatcher.signal.getValue()
        CoroutineSignalCatcher.signal.setValue(this)
    }

    override suspend fun delete() {
        CoroutineSignalCatcher.signal.setValue(parent)
    }
}