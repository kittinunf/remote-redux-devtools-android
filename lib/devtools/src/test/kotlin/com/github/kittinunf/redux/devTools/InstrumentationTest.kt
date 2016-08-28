package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.core.DevToolsOption
import com.github.kittinunf.redux.devTools.core.DevToolsStore
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

class InstrumentationTest {

    companion object {
        val TEST_PORT = 9898
        val mockSocketServer =
                object : WebSocketServer(InetSocketAddress(TEST_PORT)) {

                    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                        println("open: $handshake")
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

    lateinit var devToolsStore: DevToolsStore<CounterState>

    @Before
    fun each() {
        devToolsStore = DevToolsStore(DevToolsOption(TEST_PORT, 10), CounterState())
        devToolsStore.start()
    }

    @Test
    fun `add new state, must put current state to last`() {
        assertThat(devToolsStore.state.counter, isEqualTo(0))
        devToolsStore.handleStateChange(CounterState(1))
        assertThat(devToolsStore.state.counter, isEqualTo(1))
    }

    @Test
    fun `add multiple states, current state is the last added state`() {
        assertThat(devToolsStore.state.counter, isEqualTo(0))
        devToolsStore.handleStateChange(CounterState(1))
        devToolsStore.handleStateChange(CounterState(8))
        devToolsStore.handleStateChange(CounterState(4))
        devToolsStore.handleStateChange(CounterState(47))
        assertThat(devToolsStore.state.counter, isEqualTo(47))
    }

    @Test
    fun `send command jump to state index, make current state shifted accordingly`() {

        devToolsStore.handleStateChange(CounterState(1)) //1
        devToolsStore.handleStateChange(CounterState(8)) //2
        devToolsStore.handleStateChange(CounterState(4)) //3
        devToolsStore.handleStateChange(CounterState(47)) //4

        assertThat(devToolsStore.state.counter, isEqualTo(47))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(0.toString())
        }
        assertThat(devToolsStore.state.counter, isEqualTo(0))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(3.toString())
        }
        assertThat(devToolsStore.state.counter, isEqualTo(4))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(2.toString())
        }
        assertThat(devToolsStore.state.counter, isEqualTo(8))

        callThenWaitInSecond(2) {
            mockSocketServer.connections().first().send(1.toString())
        }
        assertThat(devToolsStore.state.counter, isEqualTo(1))
    }

    fun callThenWaitInSecond(l: Long, run: () -> Unit) {
        val count = CountDownLatch(1)
        run()
        count.await(l, TimeUnit.SECONDS)
    }

}
