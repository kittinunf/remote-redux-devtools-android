package com.github.kittinunf.redux.devTools.redux_kotlin

import org.junit.Test
import redux.api.Reducer
import redux.createStore
import java.util.concurrent.CountDownLatch

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
    fun `apply devtools in to redux-kotlin store enhancer`() {
        val countdown = CountDownLatch(1)

        val store = createStore(counterReducer(), CounterState(), devTools<CounterState>())

        store.dispatch(CounterAction.Init) // = 0
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Decrement) //-1
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Increment) //+1
        store.dispatch(CounterAction.Decrement) //-1
        store.dispatch(CounterAction.Increment) //+1

        store.subscribe {
            println(store.state)
        }

        countdown.await()
    }

}