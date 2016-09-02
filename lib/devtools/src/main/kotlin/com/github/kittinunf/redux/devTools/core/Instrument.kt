package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.socket.SocketClient
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Created by kittinunf on 8/26/16.
 */

sealed class InstrumentAction(open val type: String, open val payload: Any? = null) {

    fun toJsonObject(): JsonObject = JsonObject().apply {
        addProperty("type", type)
        buildJson()()
    }

    abstract protected fun buildJson(): JsonObject.() -> Unit

    class State(override val payload: Pair<String, String>) : InstrumentAction(type = "State") {

        constructor(json: JsonObject) : this(json["payload"].asJsonObject["state"].asString to json["payload"].asJsonObject["action"].asString)

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                add("payload", JsonObject().apply {
                    addProperty("state", payload.first)
                    addProperty("action", payload.second)
                })
            }
        }

    }

    class JumpToState(override val payload: Int) : InstrumentAction(type = "JumpToState") {

        constructor(json: JsonObject) : this(json["payload"].asInt)

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                addProperty("payload", payload)
            }
        }
    }

}

data class InstrumentOption(val host: String, val port: Int, val maxAge: Int)

fun defaultOption() = InstrumentOption("localhost", 8989, 30)

class Instrument<S>(options: InstrumentOption, initialState: S) {

    var isMonitored = false

    private var started = false

    private var currentStateIndex = -1

    private val client: SocketClient

    private val stateTimeLines = mutableListOf<S>()

    var onMessageReceived: ((S) -> Unit)? = null

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

        client.messages.subscribe { handleMessageReceived(it) }
        client.connectBlocking()
        client.send("@@INIT")
    }

    fun handleStateChangeFromAction(state: S, action: Any) {
        stateTimeLines.add(state)
        currentStateIndex = stateTimeLines.lastIndex
        val data = InstrumentAction.State(state.toString() to action.javaClass.simpleName)
        client.send(data.toJsonObject().toString())
    }

    fun stop() {
        started = false
        isMonitored = false
        client.closeBlocking()
    }

    private fun handleMessageReceived(s: String) {
        val json = JsonParser().parse(s).asJsonObject
        val index = InstrumentAction.JumpToState(json).payload
        currentStateIndex = index
        onMessageReceived?.invoke(state)
    }

}
