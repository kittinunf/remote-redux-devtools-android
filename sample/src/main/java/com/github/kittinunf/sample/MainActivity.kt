package com.github.kittinunf.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.kittinunf.sample.redux.Action
import com.github.kittinunf.sample.redux.Reducer
import com.github.kittinunf.sample.redux.State
import com.github.kittinunf.sample.redux.Store
import kotlinx.android.synthetic.main.activity_main.decreaseButton
import kotlinx.android.synthetic.main.activity_main.increaseButton
import kotlinx.android.synthetic.main.activity_main.resetButton
import kotlinx.android.synthetic.main.activity_main.resultText

data class CounterState(val count: Int = 0) : State

sealed class CounterAction : Action
object Increment : CounterAction()
object Decrement : CounterAction()
object Reset : CounterAction()

val counterReducer = object : Reducer<CounterState> {
    override fun reduce(currentState: CounterState, action: Action): CounterState = when (action) {
        Increment -> {
            currentState.copy(count = currentState.count + 1)
        }
        Decrement -> {
            currentState.copy(count = currentState.count - 1)
        }
        Reset -> {
            currentState.copy(count = 0)
        }
        else -> currentState
    }
}

class MainActivity : AppCompatActivity() {

    private val store = Store(CounterState(), counterReducer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store.states.subscribe {
            resultText.text = it.count.toString()
        }

        increaseButton.setOnClickListener { store.dispatch(Increment) }

        decreaseButton.setOnClickListener { store.dispatch(Decrement) }

        resetButton.setOnClickListener { store.dispatch(Reset) }
    }
}
