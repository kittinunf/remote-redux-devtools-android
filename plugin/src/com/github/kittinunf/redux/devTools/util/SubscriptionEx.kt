package com.github.kittinunf.redux.devTools.util

import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Created by kittinunf on 8/17/16.
 */

fun Subscription.addTo(compositeSubscription: CompositeSubscription) {
    compositeSubscription.add(this)
}
