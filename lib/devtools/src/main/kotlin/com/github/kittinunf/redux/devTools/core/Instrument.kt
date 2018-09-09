package com.github.kittinunf.redux.devTools.core

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.github.kittinunf.redux.devTools.Payload
import com.github.kittinunf.redux.devTools.socket.SocketClient
import com.github.kittinunf.redux.devTools.socket.SocketStatus
import com.google.gson.JsonParser
import io.reactivex.disposables.CompositeDisposable
import java.util.Date
import java.util.UUID

data class InstrumentOption(val host: String, val port: Int, val name: String, val maxAge: Int)

fun localHostDefaultOption(port: Int = 8989,
                           name: String = UUID.randomUUID().toString(),
                           maxAge: Int = 30) =
        InstrumentOption("localhost", port, name, maxAge)

fun emulatorDefaultOption(port: Int = 8989,
                          name: String = UUID.randomUUID().toString(),
                          maxAge: Int = 30) =
        InstrumentOption("10.0.2.2", port, name, maxAge)

class Instrument<S>(private val options: InstrumentOption, private val initialState: S) {

    var isMonitored = false

    private var started = false

    private var currentStateIndex = -1

    private val client: SocketClient = SocketClient(options.host, options.port)

    private val stateTimeLines = mutableListOf<S>()

    var onOpen: (() -> Unit)? = null
    var onError: ((ex: Exception) -> Unit)? = null
    var onMessageReceived: ((S) -> Unit)? = null

    private val subscriptions = CompositeDisposable()

    //app's view state
    var state: S = initialState
        private set
        get() {
            return stateTimeLines.getOrNull(currentStateIndex) ?: initialState
        }

    fun start(): Instrument<S> {
        if (started) return this
        isMonitored = true

        //handle message
        subscriptions.add(client.messages.subscribe {
            handleMessageReceived(it)
        })

        //handle connection
        subscriptions.add(client.connections.filter { it is SocketStatus.Open }
                .subscribe {
                    started = true
                    handleConnectionOpened()
                })

        subscriptions.add(client.connections.filter { it is SocketStatus.Error }
                .subscribe {
                    onError?.invoke((it as SocketStatus.Error).ex
                            ?: error("Socket Status Error"))
                }
        )

        return this
    }

    fun connect() = client.connect()

    fun connectBlocking() = client.connectBlocking()

    fun handleStateChangeFromAction(state: S, action: Any) {
        if (!started || !isMonitored) return

        stateTimeLines.add(state)
        val isOverMaxAgeReached = stateTimeLines.size > options.maxAge
        if (isOverMaxAgeReached) {
            stateTimeLines.removeAt(0)
        }
        currentStateIndex = stateTimeLines.lastIndex
        val data = InstrumentAction.SetState(
                Payload(state.toString(),
                        action.javaClass.simpleName,
                        isOverMaxAgeReached,
                        Date())
        )
        client.send(data.toJsonObject().toString())
    }

    fun close() {
        started = false
        isMonitored = false
        subscriptions.clear()
        client.close()
    }

    fun closeBlocking() {
        started = false
        isMonitored = false
        subscriptions.clear()
        client.closeBlocking()
    }

    private fun handleConnectionOpened() {
        onOpen?.invoke()
        val message = InstrumentAction.Init(options.name).toJsonObject().toString()
        client.send(message)
    }

    private fun handleMessageReceived(s: String) {
        val json = JsonParser().parse(s).asJsonObject
        val index = InstrumentAction.JumpToState(json).payload
        currentStateIndex = index
        onMessageReceived?.invoke(state)
    }

}
