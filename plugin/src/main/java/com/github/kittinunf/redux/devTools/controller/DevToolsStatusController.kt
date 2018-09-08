package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusAction
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusState
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
                        .filter { it["type"].asString == InstrumentAction.ActionType.INIT.name }
                        .map { DevToolsStatusAction.SetClient(it["payload"].asString) },
                SocketServer.connects.filter { it.second == SocketServer.SocketStatus.CLOSE }
                        .map { DevToolsStatusAction.SetClient("-") }
        )

        val viewModels = Observable.merge(setAddressCommand, setClientCommand)
                .scan(DevToolsStatusState()) { viewModel, command ->
                    viewModel.executeCommand(command)
                }

        viewModels.map { it.address }
                .observeOn(SwingScheduler.getInstance())
                .subscribe {
                    component.serverAddressLabel.text = it
                }
                .addTo(subscriptionBag)

        viewModels.map { it.status }
                .observeOn(SwingScheduler.getInstance())
                .subscribe {
                    component.connectedClientLabel.text = it
                }
                .addTo(subscriptionBag)
    }
}
