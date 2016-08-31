package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.*
import com.github.kittinunf.redux.devTools.core.Instrument

/**
 * Created by kittinunf on 8/25/16.
 */

object DevToolsStateChangeAction

fun <S> devTools(): StoreEnhancer<S> {
    return StoreEnhancer { storeCreator ->
        object : StoreCreator<S> {
            override val storeStandardMiddlewares: Array<out Middleware<S>> = storeCreator.storeStandardMiddlewares

            override fun <S_> ofType(): StoreCreator<S_> = storeCreator.ofType()

            override fun create(reducer: Reducer<S>, initialState: S): Store<S> {
                val store = storeCreator.create(reducer, initialState)
                val instrument = Instrument<S>(initialState = store.state)
                instrument.start()
                store.replaceReducer(Reducer<S> { s, any ->
                    val reducedState = reducer.reduce(s, any)
                    if (instrument.isMonitored && (any !is DevToolsStateChangeAction)) {
                        instrument.handleStateChangeFromAction(reducedState, any)
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


