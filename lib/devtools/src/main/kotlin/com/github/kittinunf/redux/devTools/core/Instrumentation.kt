package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.socket.SocketClient

/**
 * Created by kittinunf on 8/26/16.
 */

data class DevToolsOption(val port: Int, val maxAge: Int)

class DevToolsStore<S>(options: DevToolsOption = DevToolsOption(8989, 30), initialState: S) {

    private var started = false

    var isMonitored = true

    private var currentStateIndex = 0

    private val client: SocketClient

    private val stateTimeLines = mutableListOf<S>()

    //app's view state
    var state: S
        private set
        get() {
            return stateTimeLines[currentStateIndex]
        }

    init {
        client = SocketClient(options.port)
        state = initialState
        stateTimeLines.add(initialState)
    }

    fun start() {
        if (started) return
        started = true

        client.messages.subscribe { handleSocketMessage(it) }
        client.connectBlocking()
    }

    fun handleStateChange(state: S) {
        stateTimeLines.add(state)
        currentStateIndex = stateTimeLines.lastIndex
        client.send(state.toString())
    }

    fun stop() {
        started = false
        isMonitored = false
        client.closeBlocking()
    }

    private fun handleSocketMessage(s: String) {
        val index = s.toInt()
        currentStateIndex = index
    }

}
