package com.github.kittinunf.redux.devTools.util

object R {
    private const val images = "/images"
    private const val icon = "ic_"
    private const val png = ".png"

    const val backward = "$images/${icon}backward$png"
    const val forward = "$images/${icon}forward$png"
    const val pause = "$images/${icon}pause$png"
    const val play = "$images/${icon}play$png"
}

fun <T : Any> T.resource(resourceName: String) = javaClass.getResource(resourceName)
