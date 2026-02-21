package com.github.kshulzh.kshku.local.thread

class ThreadLocalContext<T> : LocalContext<T> {
    val map: MutableMap<Long, T?> = mutableMapOf()

    override var value: T?
        get() = map[getCurrentThread()]
        set(value) { map[getCurrentThread()] = value }
}