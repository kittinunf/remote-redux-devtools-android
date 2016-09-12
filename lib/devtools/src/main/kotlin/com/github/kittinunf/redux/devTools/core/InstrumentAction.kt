package com.github.kittinunf.redux.devTools.core

import com.google.gson.JsonObject

/**
 * Created by kittinunf on 9/5/16.
 */

sealed class InstrumentAction(open val type: String, open val payload: Any? = null) {

    enum class ActionType {
        STATE,
        JUMP_TO_STATE,
        INIT,
    }

    fun toJsonObject(): JsonObject = JsonObject().apply {
        addProperty("type", type)
        buildJson()()
    }

    abstract protected fun buildJson(): JsonObject.() -> Unit

    data class StatePayload(val state: String, val action: String, val reachMax: Boolean = false)

    class State(override val payload: StatePayload) : InstrumentAction(type = ActionType.STATE.name) {

        constructor(json: JsonObject) : this(
                StatePayload(
                        json["payload"].asJsonObject["state"].asString,
                        json["payload"].asJsonObject["action"].asString,
                        json["payload"].asJsonObject["reach_max"].asBoolean
                )
        )

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                add("payload", JsonObject().apply {
                    addProperty("state", payload.state)
                    addProperty("action", payload.action)
                    addProperty("reach_max", payload.reachMax)
                })
            }
        }

    }

    class JumpToState(override val payload: Int) : InstrumentAction(type = ActionType.JUMP_TO_STATE.name) {

        constructor(json: JsonObject) : this(json["payload"].asInt)

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                addProperty("payload", payload)
            }
        }
    }

    class Init(override val payload: String) : InstrumentAction(type = ActionType.INIT.name) {

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                addProperty("payload", payload)
            }
        }

    }

}
