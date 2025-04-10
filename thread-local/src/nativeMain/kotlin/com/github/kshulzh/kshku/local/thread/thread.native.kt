package com.github.kshulzh.kshku.local.thread

import kotlin.native.concurrent.Worker

actual fun getCurrentThread() : Long = Worker.current.id.toLong()