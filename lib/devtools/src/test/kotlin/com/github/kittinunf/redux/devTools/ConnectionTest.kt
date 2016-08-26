package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.socket.SocketClient
import org.junit.Test
import java.util.concurrent.CountDownLatch

/**
 * Created by kittinunf on 8/22/16.
 */

class ConnectionTest {

    @Test
    fun connect() {
        val countdown = CountDownLatch(1)
        var i = 0

        val socket = SocketClient()
        socket.messages.subscribe {
            i++
            println(it)
            if (i == 5) countdown.countDown()
        }
        countdown.await()
    }

}