package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.action.InstrumentAction
import com.github.kittinunf.redux.devTools.action.SetStateInstrumentAction
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
import java.awt.Rectangle
import java.text.SimpleDateFormat
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class DevToolsMonitorController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val resetItemsCommand = SocketServer.messages.map { JsonParser().parse(it).asJsonObject }
                .filter { it["type"].asString == InstrumentAction.ActionType.INIT.name }
                .map { DevToolsMonitorViewModelCommand.SetItem() }

        val addItemsCommand = SocketServer.messages.map { JsonParser().parse(it).asJsonObject }
                .filter { it["type"].asString == InstrumentAction.ActionType.STATE.name }
                .map {
                    val state = SetStateInstrumentAction(it)
                    if (state.payload.reachMax) {
                        DevToolsMonitorViewModelCommand.ShiftItem(state)
                    } else {
                        DevToolsMonitorViewModelCommand.AddItem(state)
                    }
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
                        nodes.forEachIndexed { index, item -> rootNode.add(item.makeNode(index)) }
                        model.reload()
                    } else {
                        //update with change
                        when (change) {
                            is ChangeOperation.Insert -> {
                                val newNode = nodes[change.index].makeNode(change.index)
                                model.insertNodeInto(newNode, rootNode, rootNode.childCount)
                                val y = component.monitorStateTree.preferredSize.height
                                component.monitorStateTree.scrollRectToVisible(Rectangle(0, y, 0, 0))
                            }
                        }
                    }
                    component.monitorStateTree.expandRow(0)
                }
                .addTo(subscriptionBag)
    }

}

private fun SetStateInstrumentAction.makeNode(index: Int = -1): DefaultMutableTreeNode {
    val orderString = if (index == -1) "" else "[$index]"
    val action = payload.action
    val timeStamp = payload.time

    return DefaultMutableTreeNode("$orderString $action - ${SimpleDateFormat("HH:mm:ss.SSS").format(timeStamp)}").apply {
        val leaf = DefaultMutableTreeNode(payload.state)
        add(leaf)
    }
}
