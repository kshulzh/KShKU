package com.github.kshulzh.kshku.signals

import kotlinx.coroutines.runBlocking

fun main() {
//    try {
//        SignalCatcherImpl()
//            .signal<Int> {
//                println("int $it")
//            }
//            .signal<String> {
//                println("string $it")
//            }.run {
//                c()
//            }
//    } catch (e: Throwable) {
//
//    }
//    signal(444)
    runBlocking {
            signalSuspend<Int> {
                println("int $it")
            }
            .signal<String> {
                println("string $it")
            }.run {
                sc()
            }
        signalSuspend(444)
    }
}
fun c() {
    a()
    b()
}
fun a() {
    signal(2)
}

fun b() {
    signal("5")
}

suspend fun sc() {
    sa()
    sb()
}
suspend fun sa() {
    signalSuspend(2)
    signalSuspend<Int>() {

    }
}

suspend fun sb() {
    signalSuspend("5")
}