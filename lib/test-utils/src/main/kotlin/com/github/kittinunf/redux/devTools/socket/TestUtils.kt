package com.github.kittinunf.redux.devTools.socket

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun callThenWait(seconds: Long, run: () -> Unit) {
    val count = CountDownLatch(1)
    run()
    count.await(seconds, TimeUnit.SECONDS)
}
