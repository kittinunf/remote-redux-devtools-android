package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.socket.SocketClient

/**
 * Created by kittinunf on 8/26/16.
 */

data class InstrumentOption(val host: String, val port: Int, val maxAge: Int)

class Instrument<S>(options: InstrumentOption = InstrumentOption("localhost", 8989, 30), initialState: S) {

    var isMonitored = false

    private var started = false

    private var currentStateIndex = -1

    private val client: SocketClient

    private val stateTimeLines = mutableListOf<S>()

    //app's view state
    var state: S
        private set
        get() {
            return stateTimeLines[currentStateIndex]
        }

    init {
        client = SocketClient(options.host, options.port)
        state = initialState
    }

    fun start() {
        if (started) return
        started = true
        isMonitored = true

        client.messages.subscribe { handleSocketMessage(it) }
        client.connectBlocking()
        client.send("@@INIT")
    }

    fun handleStateChangeFromAction(state: S, action: Any) {
        stateTimeLines.add(state)
        currentStateIndex = stateTimeLines.lastIndex
        client.send("{\"action_name\" : \"${action.javaClass.simpleName}\", \"state\" : \"${state.toString()}\" }")
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
