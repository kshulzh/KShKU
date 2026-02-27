package io.github.kshulzh.kshku.local.thread

actual class PlatformLocalContext<T> actual constructor(v: T?) : LocalContext<T> {
    val threadLocal: ThreadLocal<T> = ThreadLocal.withInitial { value }
    actual override var value: T?
        get() = threadLocal.get()
        set(value) { threadLocal.set(value)}
}