package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.*
import com.github.kittinunf.redux.devTools.core.Instrument
import com.github.kittinunf.redux.devTools.core.InstrumentOption
import com.github.kittinunf.redux.devTools.core.defaultOption

/**
 * Created by kittinunf on 8/25/16.
 */

object DevToolsStateChangeAction

fun <S> devTools(option: InstrumentOption = defaultOption()): StoreEnhancer<S> {
    return StoreEnhancer { storeCreator ->
        object : StoreCreator<S> {
            override val storeStandardMiddlewares: Array<out Middleware<S>> = storeCreator.storeStandardMiddlewares

            override fun <S_> ofType(): StoreCreator<S_> = storeCreator.ofType()

            override fun create(reducer: Reducer<S>, initialState: S): Store<S> {
                val store = storeCreator.create(reducer, initialState)
                val instrument = Instrument<S>(option, store.state)
                instrument.start()
                store.replaceReducer(Reducer<S> { state, action ->
                    val reducedState = reducer.reduce(state, action)
                    if (instrument.isMonitored && (action !is DevToolsStateChangeAction)) {
                        instrument.handleStateChangeFromAction(reducedState, action)
                    }
                    reducedState
                })
                return object : Store<S> {

                    override var dispatch: (Any) -> Any = {}
                        get() = store.dispatch

                    override val state: S
                        get() = instrument.state

                    override fun replaceReducer(reducer: Reducer<S>) {
                        //intentionally left blank
                    }

                    override fun subscribe(storeSubscriber: StoreSubscriber<S>): StoreSubscription =
                            store.subscribe(storeSubscriber)

                    init {
                        instrument.onMessageReceived = { store.dispatch(DevToolsStateChangeAction) }
                    }

                }
            }
        }
    }
}


