package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.state.DevToolsStatusAction
import com.github.kittinunf.redux.devTools.state.DevToolsStatusState
import com.github.kittinunf.redux.devTools.state.DevToolsStatusState.Companion.reduce
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.util.gson
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription

class DevToolsStatusController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val setAddressCommand = Observable.fromCallable { "${SocketServer.address.hostString}:${SocketServer.address.port}" }
                .map { DevToolsStatusAction.SetAddress(it.toString()) }

        val setClientCommand = Observable.merge(
                SocketServer.messages.map { gson.fromJson(it, InstrumentAction::class.java) }
                        .ofType(InstrumentAction.Init::class.java)
                        .map {
                            val payload = it.payload
                            DevToolsStatusAction.SetClient(payload)
                        },
                SocketServer.connects.filter { it.second == SocketServer.SocketStatus.CLOSE }
                        .map { DevToolsStatusAction.SetClient("-") }
        )

        val states = Observable.merge(setAddressCommand, setClientCommand)
                .scan(DevToolsStatusState(), ::reduce)

        states.map { it.address }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { component.serverAddressLabel.text = it }
                .addTo(subscriptionBag)

        states.map { it.status }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { component.connectedClientLabel.text = it }
                .addTo(subscriptionBag)
    }
}
