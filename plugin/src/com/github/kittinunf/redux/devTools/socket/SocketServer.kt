package com.github.kittinunf.redux.devTools.socket

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.InetSocketAddress

/**
 * Created by kittinunf on 8/17/16.
 */

object SocketServer : WebSocketServer(InetSocketAddress(8989)) {

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    private val connectSubject = SerializedSubject(BehaviorSubject.create<WebSocket.READYSTATE>())

    val messages = messageSubject.asObservable()
    val connects = connectSubject.asObservable()

    var hasStarted = false

    override fun onOpen(webSocket: WebSocket?, handshake: ClientHandshake?) {
        hasStarted = true
        connectSubject.onNext(webSocket?.readyState)
    }

    override fun onClose(webSocket: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        hasStarted = false
        connectSubject.onNext(webSocket?.readyState)
    }

    override fun onMessage(webSocket: WebSocket?, message: String?) {
        messageSubject.onNext(message)
    }

    override fun onError(webSocket: WebSocket?, ex: Exception?) {
    }

    fun send(message: String) {
        if (connections().isNotEmpty()) {
            connections().forEach { it.send(message) }
        }
    }

}
