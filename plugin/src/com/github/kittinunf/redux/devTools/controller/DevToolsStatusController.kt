package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.action.InstrumentAction
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusViewModel
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsStatusViewModelCommand
import com.google.gson.JsonParser
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription

/**
 * Created by kittinunf on 8/31/16.
 */

class DevToolsStatusController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val setAddressCommand = Observable.fromCallable { "${SocketServer.address.hostString}:${SocketServer.address.port}" }
                .map { DevToolsStatusViewModelCommand.SetAddress(it.toString()) }

        val setStatusCommand = Observable.merge(
                SocketServer.messages.map { JsonParser().parse(it).asJsonObject }
                        .filter { it["type"].asString == InstrumentAction.ActionType.INIT.name }
                        .map { DevToolsStatusViewModelCommand.SetStatus(it["payload"].asString) },
                SocketServer.connects.filter { it.second == SocketServer.SocketStatus.CLOSE }
                        .map { DevToolsStatusViewModelCommand.SetStatus("-") }
        )

        val viewModels = Observable.merge(setAddressCommand, setStatusCommand)
                .scan(DevToolsStatusViewModel()) { viewModel, command ->
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
                    component.serverStatusLabel.text = it
                }
                .addTo(subscriptionBag)
    }

}