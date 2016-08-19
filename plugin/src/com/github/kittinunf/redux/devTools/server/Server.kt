package com.github.kittinunf.redux.devTools.server

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.reactivex.netty.RxNetty
import io.reactivex.netty.server.RxServer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Created by kittinunf on 8/17/16.
 */

object Server {

    val DEFAULT_PORT = 8989

    private val server: RxServer<TextWebSocketFrame, TextWebSocketFrame>

    private val sendSubject = PublishSubject.create<TextWebSocketFrame>()

    private val inputSubject = SerializedSubject(BehaviorSubject.create<TextWebSocketFrame>())
    val inputs = inputSubject.asObservable()

    init {
        server = RxNetty.newWebSocketServerBuilder<TextWebSocketFrame, TextWebSocketFrame>(DEFAULT_PORT, { connection ->
            connection.input.subscribe(inputSubject)
            sendSubject.flatMap {
                connection.writeAndFlush(it)
            }
        }).build()
    }

    fun start() {
        server.start()
    }

    fun send(text: String) {
        sendSubject.onNext(TextWebSocketFrame(text))
    }

}
