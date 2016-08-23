package com.github.kittinunf.redux.devTools.server

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.InetSocketAddress

/**
 * Created by kittinunf on 8/17/16.
 */

object Server : WebSocketServer(InetSocketAddress(8989)) {

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    val messages = messageSubject.asObservable()

    override fun onOpen(webSocket: WebSocket?, p1: ClientHandshake?) {
    }

    override fun onClose(webSocket: WebSocket?, p1: Int, p2: String?, p3: Boolean) {
    }

    override fun onMessage(webSocket: WebSocket?, p1: String?) {
        messageSubject.onNext(p1)
    }

    override fun onError(p0: WebSocket?, p1: Exception?) {
    }

    fun send(msg: String) {
        connections().forEach { it.send(msg) }
    }

}
