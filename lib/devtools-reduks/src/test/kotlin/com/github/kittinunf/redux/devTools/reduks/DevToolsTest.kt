package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.Reducer
import com.beyondeye.reduks.SimpleStore
import com.beyondeye.reduks.create
import com.beyondeye.reduks.subscribe
import org.junit.Test
import java.util.concurrent.CountDownLatch

/**
 * Created by kittinunf on 8/26/16.
 */

class DevToolsTest {

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

    @Test
    fun `apply devtools in to reduks store enhancer`() {
        val countdown = CountDownLatch(1)

        val store = SimpleStore.Creator<CounterState>()
                .create(counterReducer(), CounterState(), devTools<CounterState>())

        store.subscribe {
            println(store.state)
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