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

    enum class SocketStatus {
        OPEN,
        CLOSE,
        ERROR
    }

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    private val connectSubject = SerializedSubject(BehaviorSubject.create<Pair<InetSocketAddress, SocketStatus>>())

    val messages = messageSubject.asObservable()
    val connects = connectSubject.asObservable()

    init {
        start()
    }

    override fun onOpen(webSocket: WebSocket?, handshake: ClientHandshake?) {
        connectSubject.onNext(webSocket!!.localSocketAddress to SocketStatus.OPEN)
    }

    override fun onClose(webSocket: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        connectSubject.onNext(webSocket!!.localSocketAddress to SocketStatus.CLOSE)
    }

    override fun onMessage(webSocket: WebSocket?, message: String?) {
        messageSubject.onNext(message)
    }

    override fun onError(webSocket: WebSocket?, ex: Exception?) {
        connectSubject.onNext(webSocket!!.localSocketAddress to SocketStatus.ERROR)
    }

    fun send(message: String) {
        if (connections().isNotEmpty()) {
            connections().forEach { it.send(message) }
        }
    }

}
