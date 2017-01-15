package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.core.Instrument
import com.github.kittinunf.redux.devTools.core.InstrumentAction
import com.github.kittinunf.redux.devTools.core.InstrumentOption
import org.hamcrest.MatcherAssert.assertThat
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is` as isEqualTo

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


    @Test
    fun `add new state, must put current state to last`() {
        val instrument = Instrument(InstrumentOption("localhost", TEST_PORT, "TEST_CLIENT", 10), CounterState())
        instrument.start()
        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        assertThat(instrument.state.counter, isEqualTo(1))
        instrument.stop()
    }

    @Test
    fun `add multiple states, current state is the last added state`() {
        val instrument = Instrument(InstrumentOption("localhost", TEST_PORT, "TEST_CLIENT", 10), CounterState())
        instrument.start()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment)

        assertThat(instrument.state.counter, isEqualTo(47))
        instrument.stop()
    }

    @Test
    fun `send command jump to state index, make current state shifted accordingly`() {
        val instrument = Instrument(InstrumentOption("localhost", TEST_PORT, "TEST_CLIENT", 10), CounterState())
        instrument.start()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment) //0
        instrument.handleStateChangeFromAction(CounterState(8), CounterAction.Increment) //1
        instrument.handleStateChangeFromAction(CounterState(4), CounterAction.Decrement) //2
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment) //3

        //first, state is equal to latest change
        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(1))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(3).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(2).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(4))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(1).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(8))

        instrument.stop()
    }

    fun callThenWaitInSecond(l: Long, run: () -> Unit) {
        val count = CountDownLatch(1)
        run()
        count.await(l, TimeUnit.SECONDS)
    }

    @Test
    fun `state is shifted when the max age config is reached`() {
        val instrument = Instrument(InstrumentOption("localhost", TEST_PORT, "TEST_CLIENT", 5), CounterState())

        instrument.start()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment) //0
        instrument.handleStateChangeFromAction(CounterState(8), CounterAction.Increment) //1
        instrument.handleStateChangeFromAction(CounterState(4), CounterAction.Decrement) //2
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment) //3
        instrument.handleStateChangeFromAction(CounterState(16), CounterAction.Decrement) //4

        //first, state is equal to latest change
        assertThat(instrument.state.counter, isEqualTo(16))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        //at 0 index, counter is equal to 1
        assertThat(instrument.state.counter, isEqualTo(1))

        //the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(27), CounterAction.Increment) //5
        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(8))

        //again, the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(3), CounterAction.Decrement) //6
        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(4))

        //again, the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(10), CounterAction.Increment) //7
        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(47))
    }

}
