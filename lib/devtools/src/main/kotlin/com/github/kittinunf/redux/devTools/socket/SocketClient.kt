package com.github.kittinunf.redux.devTools.socket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.URI

/**
 * Created by kittinunf on 8/22/16.
 */

class SocketClient(host: String = "localhost", port: Int = 8989) {

    private val PATH = "ws://$host:$port"

    private val client: WebSocketClient

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    val messages = messageSubject.asObservable()

    init {
        client = object : WebSocketClient(URI(PATH)) {
            override fun onOpen(handshake: ServerHandshake?) {
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
            }

            override fun onMessage(message: String?) {
                messageSubject.onNext(message)
            }

            override fun onError(ex: Exception?) {
            }
        }
    }

    fun connect() {
        client.connect()
    }

    fun connectBlocking() {
        client.connectBlocking()
    }

    fun send(msg: String) {
        client.send(msg)
    }

    fun close() {
        client.close()
    }

    fun closeBlocking() {
        client.closeBlocking()
    }

}