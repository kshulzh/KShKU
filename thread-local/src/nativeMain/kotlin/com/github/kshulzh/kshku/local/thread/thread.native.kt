package com.github.kshulzh.kshku.local.thread

import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.Worker

@OptIn(ObsoleteWorkersApi::class)
actual fun getCurrentThread() : Long = Worker.current.id.toLong()