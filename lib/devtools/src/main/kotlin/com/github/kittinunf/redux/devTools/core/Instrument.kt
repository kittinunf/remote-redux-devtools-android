package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.socket.SocketClient
import com.google.gson.JsonParser
import java.util.*

data class InstrumentOption(val host: String, val port: Int, val name: String, val maxAge: Int)

fun defaultOption() = InstrumentOption("localhost", 8989, UUID.randomUUID().toString(), 30)

class Instrument<S>(val options: InstrumentOption, val initialState: S) {

    var isMonitored = false

    private var started = false

    private var currentStateIndex = -1

    private val client: SocketClient = SocketClient(options.host, options.port)

    private val stateTimeLines = mutableListOf<S>()

    var onConnectionOpened: (() -> Unit)? = null
    var onMessageReceived: ((S) -> Unit)? = null

    //app's view state
    var state: S = initialState
        private set
        get() {
            return stateTimeLines.getOrNull(currentStateIndex) ?: initialState
        }

    fun start() {
        if (started) return
        isMonitored = true

        //handle message
        client.messages.subscribe {
            handleMessageReceived(it)
        }

        //handle connection
        client.connects.filter { it == SocketClient.SocketStatus.OPEN }
                .subscribe {
                    handleConnectionOpened()
                }

        started = client.connectBlocking()
    }

    fun handleStateChangeFromAction(state: S, action: Any) {
        if (!started) start()

        stateTimeLines.add(state)
        val isOverMaxAgeReached = stateTimeLines.size > options.maxAge
        if (isOverMaxAgeReached) {
            stateTimeLines.removeAt(0)
        }
        currentStateIndex = stateTimeLines.lastIndex
        val data = InstrumentAction.State(
                InstrumentAction.StatePayload(state.toString(), action.javaClass.simpleName, isOverMaxAgeReached)
        )
        client.send(data.toJsonObject().toString())
    }

    fun stop() {
        started = false
        isMonitored = false
        client.closeBlocking()
    }

    private fun handleConnectionOpened() {
        val message = InstrumentAction.Init(options.name).toJsonObject().toString()
        client.send(message)
        onConnectionOpened?.invoke()
    }

    private fun handleMessageReceived(s: String) {
        val json = JsonParser().parse(s).asJsonObject
        val index = InstrumentAction.JumpToState(json).payload
        currentStateIndex = index
        onMessageReceived?.invoke(state)
    }

}