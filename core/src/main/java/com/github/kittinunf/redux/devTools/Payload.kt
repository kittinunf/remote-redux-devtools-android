package com.github.kittinunf.redux.devTools

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
