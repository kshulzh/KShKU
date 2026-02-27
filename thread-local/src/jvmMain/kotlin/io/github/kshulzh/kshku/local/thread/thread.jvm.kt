package io.github.kshulzh.kshku.local.thread

actual fun getCurrentThread() : Long = Thread.currentThread().threadId()