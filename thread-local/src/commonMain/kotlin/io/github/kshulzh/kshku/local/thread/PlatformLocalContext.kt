package io.github.kshulzh.kshku.local.thread

expect class PlatformLocalContext<T> (v: T?) : LocalContext<T> {
    override var value: T?
}