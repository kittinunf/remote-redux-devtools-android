package com.github.kittinunf.redux.devTools.client

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.reactivex.netty.RxNetty
import io.reactivex.netty.protocol.http.websocket.WebSocketClient
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Created by kittinunf on 8/17/16.
 */

object Client {

    val DEFAULT_PORT = 8989

    private val client: WebSocketClient<TextWebSocketFrame, TextWebSocketFrame>

    private val sendSubject = PublishSubject.create<TextWebSocketFrame>()

    private val inputSubject = SerializedSubject(BehaviorSubject.create<TextWebSocketFrame>())
    val inputs = inputSubject.asObservable()

    init {
        client = RxNetty.newWebSocketClientBuilder<TextWebSocketFrame, TextWebSocketFrame>("localhost", DEFAULT_PORT)
                .withWebSocketVersion(WebSocketVersion.V13)
                .build()
    }

    fun connect(): Subscription {
        return client.connect()
                .flatMap { connection ->
                    connection.input.subscribe(inputSubject)
                    sendSubject.flatMap {
                        connection.writeAndFlush(it)
                    }
                }
                .publish()
                .connect()
    }

    fun send(text: String) {
        sendSubject.onNext(TextWebSocketFrame(text))
    }

}
