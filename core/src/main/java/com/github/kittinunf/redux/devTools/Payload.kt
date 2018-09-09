package com.github.kittinunf.redux.devTools

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.util.Date

data class Payload(
        @SerializedName("state")
        val state: String,
        @SerializedName("action")
        val action: String,
        @SerializedName("reach_max")
        val reachMax: Boolean,
        @SerializedName("time")
        val time: Date
)

private val dateSerializer = JsonSerializer<Date> { src, _, _ ->
    JsonPrimitive(src.time)
}

private val dateDeserializer = JsonDeserializer<Date> { json, _, _ ->
    Date(json.asLong)
}

val gson = GsonBuilder().registerTypeAdapter(Date::class.java, dateSerializer)
        .registerTypeAdapter(Date::class.java, dateDeserializer)
        .create()
