package com.github.kittinunf.redux.devTools.action

import com.google.gson.JsonObject

/**
 * Created by kittinunf on 8/30/16.
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

