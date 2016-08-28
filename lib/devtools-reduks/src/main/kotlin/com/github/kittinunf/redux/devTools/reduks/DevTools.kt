package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.*
import com.github.kittinunf.redux.devTools.core.DevToolsStore

/**
 * Created by kittinunf on 8/25/16.
 */

fun <S> devTools(): StoreEnhancer<S> {
    return StoreEnhancer { storeCreator ->
        object : StoreCreator<S> {
            override val storeStandardMiddlewares: Array<out Middleware<S>> = storeCreator.storeStandardMiddlewares

            override fun <S_> ofType(): StoreCreator<S_> = storeCreator.ofType()

            override fun create(reducer: Reducer<S>, initialState: S): Store<S> {
                val store = storeCreator.create(reducer, initialState)
                val devToolStore = DevToolsStore<S>(store.state)
                devToolStore.start()
                store.subscribe(StoreSubscriber {
                    if (devToolStore.isMonitored) devToolStore.handleStateChange(store.state)
                })
                return object : Store<S> {
                    override var dispatch: (Any) -> Any = store.dispatch

                    override val state: S = devToolStore.state

                    override fun replaceReducer(reducer: Reducer<S>) {
                        store.replaceReducer(reducer)
                    }

                    override fun subscribe(storeSubscriber: StoreSubscriber<S>): StoreSubscription = store.subscribe(storeSubscriber)
                }
            }
        }
    }
}


