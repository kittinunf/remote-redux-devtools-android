package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.action.InstrumentAction
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.viewmodel.ChangeOperation
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsMonitorViewModel
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsMonitorViewModelCommand
import com.google.gson.JsonParser
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Created by kittinunf on 8/19/16.
 */

class DevToolsMonitorController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val resetItemsCommand = SocketServer.messages.filter { it == "@@INIT" }.map { DevToolsMonitorViewModelCommand.SetItem() }
        val addItemsCommand = SocketServer.messages.filter { it != "@@INIT" }.map {
            val state = InstrumentAction.State(JsonParser().parse(it).asJsonObject)
            DevToolsMonitorViewModelCommand.AddItem(state)
        }

        val viewModels = Observable.merge(resetItemsCommand, addItemsCommand)
                .scan(DevToolsMonitorViewModel()) { viewModel, command ->
                    viewModel.executeCommand(command)
                }

        viewModels.map { viewModel -> viewModel.change to viewModel.items }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { data ->
                    val (change, nodes) = data
                    val model = component.monitorStateTree.model as DefaultTreeModel
                    val rootNode = model.root as DefaultMutableTreeNode
                    if (change == null) {
                        //reload
                        rootNode.removeAllChildren()
                        nodes.forEach { rootNode.add(it.makeNode()) }
                        model.reload()
                    } else {
                        //update with change
                        when (change) {
                            is ChangeOperation.Insert -> {
                                val newNode = nodes[change.index].makeNode()
                                model.insertNodeInto(newNode, rootNode, rootNode.childCount)
                            }
                        }
                    }
                    component.monitorStateTree.expandRow(0)
                }
                .addTo(subscriptionBag)
    }

}

fun InstrumentAction.State.makeNode(): DefaultMutableTreeNode {
    return DefaultMutableTreeNode(payload.second).apply {
        val leaf = DefaultMutableTreeNode(payload.first)
        add(leaf)
    }
}
