package com.github.kittinunf.redux.devTools.socket

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class MockSocketServer(port: Int) : WebSocketServer(InetSocketAddress(port)) {

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        println("open: ${handshake?.resourceDescriptor}")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("close: $code, $reason")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        println("message: $message")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        println("error: ${ex?.message}")
    }

}