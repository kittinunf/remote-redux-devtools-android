package com.github.kittinunf.redux.devTools.action

import com.google.gson.JsonObject
import java.util.Date

data class Payload(val state: String, val action: String, val reachMax: Boolean = false, val time: Date = Date())

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

    protected abstract fun buildJson(): JsonObject.() -> Unit

    class Init(override val payload: String) : InstrumentAction(type = ActionType.INIT.name) {

        override fun buildJson(): JsonObject.() -> Unit {
            return {
                addProperty("payload", payload)
            }
        }
    }

    class SetState(override val payload: Payload) : InstrumentAction(type = ActionType.STATE.name) {

        constructor(json: JsonObject) : this(
                Payload(
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
}



