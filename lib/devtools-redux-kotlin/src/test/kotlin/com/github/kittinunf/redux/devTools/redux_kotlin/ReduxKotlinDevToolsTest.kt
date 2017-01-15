package com.github.kittinunf.redux.devTools.redux_kotlin

import com.github.kittinunf.redux.devTools.core.InstrumentOption
import com.github.kittinunf.redux.devTools.socket.MockSocketServer
import org.hamcrest.MatcherAssert.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import redux.api.Reducer
import redux.createStore
import java.util.*
import java.util.concurrent.CountDownLatch
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class ReduxKotlinDevToolsTest {

    companion object {

        val mockSocketServer = MockSocketServer(9898)

        @BeforeClass @JvmStatic
        fun once() {
            mockSocketServer.start()
        }

        @AfterClass @JvmStatic
        fun destroy() {
            mockSocketServer.stop()
        }

    }

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

    fun testInstrumentOption() = InstrumentOption("localhost", 9898, UUID.randomUUID().toString(), 30)

    @Test
    fun `apply devtools in to redux-kotlin store enhancer`() {
        val countdown = CountDownLatch(1)

        val store = createStore(counterReducer(), CounterState(), devTools<CounterState>(testInstrumentOption()))

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

}