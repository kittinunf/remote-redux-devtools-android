package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.core.InstrumentOption
import com.github.kittinunf.redux.devTools.core.Instrument
import org.hamcrest.MatcherAssert.assertThat
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is` as isEqualTo

/**
 * Created by kittinunf on 8/28/16.
 */

class InstrumentTest {

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

    data class CounterState(val counter: Int = 0)

    sealed class CounterAction() {
        object Increment
        object Decrement
    }

    lateinit var instrument: Instrument<CounterState>

    @Before
    fun each() {
        instrument = Instrument(InstrumentOption(TEST_PORT, 10), CounterState())
        instrument.start()
    }

    @Test
    fun `add new state, must put current state to last`() {
        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        assertThat(instrument.state.counter, isEqualTo(1))
    }

    @Test
    fun `add multiple states, current state is the last added state`() {
        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment)
        assertThat(instrument.state.counter, isEqualTo(47))
    }

    @Test
    fun `send command jump to state index, make current state shifted accordingly`() {

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment) //0
        instrument.handleStateChangeFromAction(CounterState(8), CounterAction.Increment) //1
        instrument.handleStateChangeFromAction(CounterState(4), CounterAction.Increment) //2
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment) //3

        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(0.toString())
        }
        assertThat(instrument.state.counter, isEqualTo(1))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(3.toString())
        }
        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(2.toString())
        }
        assertThat(instrument.state.counter, isEqualTo(4))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(1.toString())
        }
        assertThat(instrument.state.counter, isEqualTo(8))
    }

    fun callThenWaitInSecond(l: Long, run: () -> Unit) {
        val count = CountDownLatch(1)
        run()
        count.await(l, TimeUnit.SECONDS)
    }

}
