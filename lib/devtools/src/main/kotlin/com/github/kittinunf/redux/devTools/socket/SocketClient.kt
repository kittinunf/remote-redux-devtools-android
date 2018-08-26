package com.github.kittinunf.redux.devTools.socket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.URI

sealed class SocketStatus {
    class Open(val handshakedata: ServerHandshake?) : SocketStatus()
    class Close(val code: Int, val reason: String?) : SocketStatus()
    class Error(val ex: Exception?) : SocketStatus()
}

class SocketClient(host: String = "localhost", port: Int = 8989) : WebSocketClient(URI("ws://$host:$port")) {

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    private val connectSubject = SerializedSubject(BehaviorSubject.create<SocketStatus>())

    val messages: Observable<String> by lazy { messageSubject.asObservable() }
    val connections: Observable<SocketStatus> by lazy { connectSubject.asObservable() }

    init {
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        connectSubject.onNext(SocketStatus.Open(handshakedata))
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        connectSubject.onNext(SocketStatus.Close(code, reason))
    }

    override fun onMessage(message: String?) {
        messageSubject.onNext(message)
    }

    override fun onError(ex: Exception?) {
        connectSubject.onNext(SocketStatus.Error(ex))
    }

}
