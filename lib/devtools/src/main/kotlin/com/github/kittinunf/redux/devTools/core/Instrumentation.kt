package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.socket.SocketClient

/**
 * Created by kittinunf on 8/26/16.
 */

val client = SocketClient()
var started = false

var isMonitored = true

fun start() {
    if (started) return
    started = true

    client.messages.subscribe(::handleSocketMessage)
    client.connectBlocking()
}

fun <S> handleStateChange(state: S, stateTimeLines: MutableList<S>) {
    stateTimeLines.add(state)
    client.send(state.toString())

    println(stateTimeLines)
}

private fun stop() {
    started = false
    isMonitored = false
}

private fun handleSocketMessage(s: String) {
    println("Incoming message: $s")
}
