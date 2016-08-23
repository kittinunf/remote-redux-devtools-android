package com.github.kittinunf.redux.devTools

import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.URI

/**
 * Created by kittinunf on 8/22/16.
 */

object Client {

    private val client: WebSocketClient

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    val messages = messageSubject.asObservable()

    init {
        client = object : WebSocketClient(URI("ws://localhost:8989")) {
            override fun onOpen(p0: ServerHandshake?) {
                println(p0)
            }

            override fun onClose(p0: Int, p1: String?, p2: Boolean) {
            }

            override fun onMessage(p0: String?) {
                messageSubject.onNext(p0)
            }

            override fun onError(p0: Exception?) {
            }
        }
    }

    fun connect() {
        client.connect()
    }

    fun send(msg: String) {
        client.send(msg)
    }

}