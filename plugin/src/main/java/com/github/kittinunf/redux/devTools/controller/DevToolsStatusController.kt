package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusAction
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusState
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusState.Companion.reduce
import com.google.gson.JsonParser
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription

class DevToolsStatusController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val setAddressCommand = Observable.fromCallable { "${SocketServer.address.hostString}:${SocketServer.address.port}" }
                .map { DevToolsStatusAction.SetAddress(it.toString()) }

        val setClientCommand = Observable.merge(
                SocketServer.messages.map { JsonParser().parse(it).asJsonObject }
                        .filter { it["type"].asString == InstrumentAction.Type.INIT.name }
                        .map {
                            val payload = InstrumentAction.Init(it).payload
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
