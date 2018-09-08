package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.core.Instrument
import com.github.kittinunf.redux.devTools.core.InstrumentAction
import com.github.kittinunf.redux.devTools.core.localHostDefaultOption
import com.github.kittinunf.redux.devTools.socket.MockSocketServer
import com.github.kittinunf.redux.devTools.socket.callThenWait
import org.hamcrest.MatcherAssert.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class InstrumentTest {

    companion object {
        val TEST_PORT = 9898
        private val mockServer = MockSocketServer(9898)

        @BeforeClass
        @JvmStatic
        fun once() {
            mockServer.start()
        }

        @AfterClass
        @JvmStatic
        fun destroy() {
            mockServer.stop()
        }
    }

    data class CounterState(val counter: Int = 0)

    sealed class CounterAction {
        object Increment
        object Decrement
    }

    @Test
    fun `add new state, must put current state to last`() {
        val instrument = createInstrument(10)
        instrument.start()
        instrument.connectBlocking()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        assertThat(instrument.state.counter, isEqualTo(1))
        instrument.closeBlocking()
    }

    @Test
    fun `add multiple states, current state is the last added state`() {
        val instrument = createInstrument(10)
        instrument.start()
        instrument.connectBlocking()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment)
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment)

        assertThat(instrument.state.counter, isEqualTo(47))
        instrument.closeBlocking()
    }

    @Test
    fun `add multiple states, stop monitoring, then the current state should be calculated correctly`() {

    }

    @Test
    fun `send command jump to state index, make current state shifted accordingly`() {
        val instrument = createInstrument(10)
        instrument.start()
        instrument.connectBlocking()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment) //0
        instrument.handleStateChangeFromAction(CounterState(8), CounterAction.Increment) //1
        instrument.handleStateChangeFromAction(CounterState(4), CounterAction.Decrement) //2
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment) //3

        //first, state is equal to latest change
        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(1))

        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(3).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(47))

        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(2).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(4))

        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(1).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(8))

        instrument.closeBlocking()
    }

    @Test
    fun `state is shifted when the max age config is reached`() {
        val instrument = createInstrument(5)

        instrument.start()
        instrument.connectBlocking()

        instrument.handleStateChangeFromAction(CounterState(1), CounterAction.Increment) //0
        instrument.handleStateChangeFromAction(CounterState(8), CounterAction.Increment) //1
        instrument.handleStateChangeFromAction(CounterState(4), CounterAction.Decrement) //2
        instrument.handleStateChangeFromAction(CounterState(47), CounterAction.Increment) //3
        instrument.handleStateChangeFromAction(CounterState(16), CounterAction.Decrement) //4

        //first, state is equal to latest change
        assertThat(instrument.state.counter, isEqualTo(16))

        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        //at 0 index, counter is equal to 1
        assertThat(instrument.state.counter, isEqualTo(1))

        //the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(27), CounterAction.Increment) //5
        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(8))

        //again, the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(3), CounterAction.Decrement) //6
        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(4))

        //again, the oldest one gets remove
        instrument.handleStateChangeFromAction(CounterState(10), CounterAction.Increment) //7
        callThenWait(2) {
            mockServer.connections().first().send(InstrumentAction.JumpToState(0).toJsonObject().toString())
        }
        assertThat(instrument.state.counter, isEqualTo(47))
    }

    private fun createInstrument(maxAge: Int) = Instrument(localHostDefaultOption(TEST_PORT, "TEST", maxAge), CounterState())
}
