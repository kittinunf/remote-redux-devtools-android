package com.github.kittinunf.redux.devTools.util

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.util.Date

private val dateSerializer = JsonSerializer<Date> { src, _, _ ->
    JsonPrimitive(src.time)
}

private val dateDeserializer = JsonDeserializer<Date> { json, _, _ ->
    Date(json.asLong)
}

private val instrumentActionSerializer = JsonSerializer<InstrumentAction> { src, typeOfSrc, context ->
    src.toJsonObject()
}

private val instrumentActionDeserializer = JsonDeserializer<InstrumentAction> { json, typeOfT, context ->
    val jsonObject = json.asJsonObject
    when (enumValueOf<InstrumentAction.Type>(jsonObject["type"].asString)) {
        InstrumentAction.Type.INIT -> InstrumentAction.Init(jsonObject)
        InstrumentAction.Type.JUMP_TO_STATE -> InstrumentAction.JumpToState(jsonObject)
        InstrumentAction.Type.SET_STATE -> InstrumentAction.SetState(jsonObject)
    }
}

val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, dateSerializer)
        .registerTypeAdapter(Date::class.java, dateDeserializer)
        .registerTypeAdapter(InstrumentAction::class.java, instrumentActionSerializer)
        .registerTypeAdapter(InstrumentAction::class.java, instrumentActionDeserializer)
        .create()
