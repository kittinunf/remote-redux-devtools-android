package com.github.kittinunf.redux.devTools.reduks

import com.beyondeye.reduks.*
import com.github.kittinunf.redux.devTools.core.isMonitored
import com.github.kittinunf.redux.devTools.core.handleStateChange
import com.github.kittinunf.redux.devTools.core.start

/**
 * Created by kittinunf on 8/25/16.
 */

fun <S> devTools(): StoreEnhancer<S> {
    return StoreEnhancer { storeCreator ->
        object : StoreCreator<S> {

            val stateTimeLines = mutableListOf<S>()

            override val storeStandardMiddlewares: Array<out Middleware<S>> = storeCreator.storeStandardMiddlewares

            override fun <S_> ofType(): StoreCreator<S_> = storeCreator.ofType()

            override fun create(reducer: Reducer<S>, initialState: S): Store<S> {
                val store = storeCreator.create(reducer, initialState)
                start()
                store.subscribe(StoreSubscriber {
                    if (isMonitored) handleStateChange(store.state, stateTimeLines)
                })
                return store
            }
        }
    }
}


