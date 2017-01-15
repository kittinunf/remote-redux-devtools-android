package com.github.kittinunf.redux.devTools.redux_kotlin

import com.github.kittinunf.redux.devTools.core.Instrument
import com.github.kittinunf.redux.devTools.core.InstrumentOption
import com.github.kittinunf.redux.devTools.core.defaultOption
import redux.INIT
import redux.api.Reducer
import redux.api.Store

object DevToolsStateChangeAction

fun <S> devTools(option: InstrumentOption = defaultOption()): Store.Enhancer<S> {
    return Store.Enhancer { storeCreator ->
        Store.Creator<S> { reducer, initialState ->
            val store = storeCreator.create(reducer, initialState)
            val instrument = Instrument<S>(option, store.state)
            instrument.start()
            store.replaceReducer { state, action ->
                if (action != INIT) {
                    val reducedState = reducer.reduce(state, action)
                    if (instrument.isMonitored && (action !is DevToolsStateChangeAction)) {
                        instrument.handleStateChangeFromAction(reducedState, action)
                    }
                    reducedState
                } else {
                    state
                }
            }
            object : Store<S> {

                override fun dispatch(action: Any?): Any = store.dispatch(action)

                override fun getState(): S = instrument.state

                override fun replaceReducer(reducer: Reducer<S>?) {
                    //intentionally left blank
                }

                override fun subscribe(subscriber: Store.Subscriber?): Store.Subscription
                        = store.subscribe(subscriber)

                init {
                    instrument.onMessageReceived = {
                        store.dispatch(DevToolsStateChangeAction)
                    }
                }

            }
        }

    }
}


