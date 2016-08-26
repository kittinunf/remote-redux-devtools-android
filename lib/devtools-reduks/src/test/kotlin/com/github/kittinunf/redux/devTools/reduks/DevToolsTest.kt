package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.Reducer
import com.beyondeye.reduks.SimpleStore
import com.beyondeye.reduks.create
import org.junit.Test
import java.util.concurrent.CountDownLatch

/**
 * Created by kittinunf on 8/26/16.
 */

class DevToolsTest {

    data class CounterState(val count: Int = 0)

    sealed class CounterAction {
        object Init : CounterAction()
        object Increment : CounterAction()
        object Decrement : CounterAction()
    }

    val counterReducer = Reducer<CounterState> { state, action ->
        when (action) {
            is CounterAction.Init -> CounterState()
            is CounterAction.Increment -> {
                state.copy(count = state.count + 1)
            }
            is CounterAction.Decrement -> {
                state.copy(count = state.count - 1)
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    @Test
    fun testApplyDevTools() {
        val countdown = CountDownLatch(1)
        val store = SimpleStore.Creator<CounterState>()
                .create(counterReducer, CounterState(), devTools<CounterState>())

        store.dispatch(CounterAction.Increment)
        store.dispatch(CounterAction.Decrement)
        store.dispatch(CounterAction.Increment)
        store.dispatch(CounterAction.Increment)
        store.dispatch(CounterAction.Decrement)
        store.dispatch(CounterAction.Increment)

        countdown.await()
    }

}