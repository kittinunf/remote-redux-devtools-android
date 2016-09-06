package com.github.kittinunf.redux.devTools.socket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import java.net.URI

/**
 * Created by kittinunf on 8/22/16.
 */

class SocketClient(host: String = "localhost", port: Int = 8989) : WebSocketClient(URI("ws://$host:$port")) {

    enum class SocketStatus {
        OPEN,
        CLOSE,
        ERROR
    }

    private val messageSubject = SerializedSubject(BehaviorSubject.create<String>())
    private val connectSubject = SerializedSubject(BehaviorSubject.create<SocketStatus>())

    val messages: Observable<String> by lazy { messageSubject.asObservable() }
    val connects: Observable<SocketStatus> by lazy { connectSubject.asObservable() }

    init {
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        connectSubject.onNext(SocketStatus.OPEN)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        connectSubject.onNext(SocketStatus.CLOSE)
    }

    override fun onMessage(message: String?) {
        messageSubject.onNext(message)
    }

    override fun onError(ex: Exception?) {
        connectSubject.onNext(SocketStatus.ERROR)
    }

}