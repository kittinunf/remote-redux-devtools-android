package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.Reducer
import com.beyondeye.reduks.SimpleStore
import com.beyondeye.reduks.create
import com.beyondeye.reduks.subscribe
import com.github.kittinunf.redux.devTools.core.InstrumentOption
import org.hamcrest.MatcherAssert.assertThat
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class ReduksDevToolsTest {

    data class CounterState(val count: Int = 0)

    sealed class CounterAction {
        object Init
        object Increment
        object Decrement
    }

    fun counterReducer() = Reducer<CounterState> { state, action ->
        when (action) {
            is CounterAction.Init -> CounterState()
            is CounterAction.Increment -> {
                state.copy(count = state.count + 1)
            }
            is CounterAction.Decrement -> {
                state.copy(count = state.count - 1)
            }
            else -> state
        }
    }

    fun testInstrumentOption() = InstrumentOption("localhost", TEST_PORT, UUID.randomUUID().toString(), 30)

    @Test
    fun `apply devtools in to reduks store enhancer`() {
        val countdown = CountDownLatch(1)

        val store = SimpleStore.Creator<CounterState>()
                .create(counterReducer(), CounterState(), devTools<CounterState>(testInstrumentOption()))

        var count = 0
        store.subscribe {
            count++
            when (count) {
                1 -> assertThat(store.state.count, isEqualTo(0))
                2 -> assertThat(store.state.count, isEqualTo(1))
                3 -> assertThat(store.state.count, isEqualTo(0))
                4 -> assertThat(store.state.count, isEqualTo(1))
                5 -> assertThat(store.state.count, isEqualTo(2))
                6 -> assertThat(store.state.count, isEqualTo(1))
                7 -> {
                    assertThat(store.state.count, isEqualTo(2))
                    countdown.countDown()
                }
            }
        }

        store.dispatch(CounterAction.Init) // = 0
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Decrement) //-1
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Decrement) //-1
        store.dispatch(CounterAction.Increment) //+1

        countdown.await()
    }

    companion object {
        val TEST_PORT = 9898
        val mockSocketServer = object : WebSocketServer(InetSocketAddress(TEST_PORT)) {

            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                println("open: ${handshake?.resourceDescriptor}")
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                println("close: $code, $reason")
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
                println("message: $message")
            }

            override fun onError(conn: WebSocket?, ex: Exception?) {
                println("error: ${ex?.message}")
            }

        }

        @BeforeClass @JvmStatic
        fun once() {
            mockSocketServer.start()
        }

        @AfterClass @JvmStatic
        fun destroy() {
            mockSocketServer.stop()
        }

    }

}