package io.github.kshulzh.kshku.local.thread

actual class PlatformLocalContext<T> actual constructor(var v: T?) : LocalContext<T> {
    actual override var value: T? = v
}
