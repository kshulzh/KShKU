package com.github.kshulzh.kshku.signals

import kotlin.reflect.KClass

class SignalCatcherImpl : SignalCatcher {
    var parent: SignalCatcher? = null
    val map:MutableMap<KClass<*>, (Any) -> Unit> = linkedMapOf()
    override fun <T : Any> addCatcher(kClass: KClass<*>, body: (T) -> Unit) {
        map[kClass] = body as (Any) -> Unit
    }

    override fun <T> handle(obj: T) {
        map.entries.find {
            it.key.isInstance(obj)
        }?.value?.let { it(obj as Any) } ?: parent?.handle(obj)
    }

    override fun setup() {
        parent = SignalCatcher.signal
        SignalCatcher.signal = this
    }

    override fun delete() {
        SignalCatcher.signal = parent
    }
}