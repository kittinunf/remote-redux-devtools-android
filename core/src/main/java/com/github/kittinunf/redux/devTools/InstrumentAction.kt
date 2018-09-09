package com.github.kittinunf.redux.devTools

import com.github.kittinunf.redux.devTools.util.gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

sealed class InstrumentAction(val type: String, open val payload: Any? = null) {

    enum class Type {
        SET_STATE,
        JUMP_TO_STATE,
        INIT,
    }

    fun toJsonObject(): JsonObject = JsonObject().apply {
        addProperty("type", type)
        add("payload", createPayloadJson())
    }

    protected abstract fun createPayloadJson(): JsonElement

    class Init(override val payload: String) : InstrumentAction(type = Type.INIT.name) {

        constructor(json: JsonObject) : this(json["payload"].asString)

        override fun createPayloadJson(): JsonElement = gson.toJsonTree(payload)
    }

    class SetState(override val payload: Payload) : InstrumentAction(type = Type.SET_STATE.name) {

        constructor(json: JsonObject) : this(gson.fromJson(json["payload"], Payload::class.java))

        override fun createPayloadJson(): JsonElement = gson.toJsonTree(payload, Payload::class.java)
    }

    class JumpToState(override val payload: Int) : InstrumentAction(type = Type.JUMP_TO_STATE.name) {

        constructor(json: JsonObject) : this(json["payload"].asInt)

        override fun createPayloadJson(): JsonElement = gson.toJsonTree(payload)
    }
}
