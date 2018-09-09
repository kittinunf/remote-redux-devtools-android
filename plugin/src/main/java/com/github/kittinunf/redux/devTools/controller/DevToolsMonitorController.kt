package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.InstrumentAction
import com.github.kittinunf.redux.devTools.Payload
import com.github.kittinunf.redux.devTools.socket.SocketServer
import com.github.kittinunf.redux.devTools.state.ChangeOperation
import com.github.kittinunf.redux.devTools.state.DevToolsMonitorAction
import com.github.kittinunf.redux.devTools.state.DevToolsMonitorState
import com.github.kittinunf.redux.devTools.state.DevToolsMonitorState.Companion.reduce
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.util.gson
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription
import java.awt.Rectangle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class DevToolsMonitorController(component: DevToolsPanelComponent) {

    private val subscriptionBag = CompositeSubscription()

    init {
        val resetItemsCommand = SocketServer.messages.map { gson.fromJson(it, InstrumentAction::class.java) }
                .ofType(InstrumentAction.Init::class.java)
                .map { DevToolsMonitorAction.SetItem() }

        val addItemsCommand = SocketServer.messages.map { gson.fromJson(it, InstrumentAction::class.java) }
                .ofType(InstrumentAction.SetState::class.java)
                .map {
                    val payload = it.payload
                    if (payload.reachMax) {
                        DevToolsMonitorAction.ShiftItem(payload)
                    } else {
                        DevToolsMonitorAction.AddItem(payload)
                    }
                }

        val states = Observable.merge(resetItemsCommand, addItemsCommand)
                .scan(DevToolsMonitorState(), ::reduce)

        states.map { viewModel -> viewModel.change to viewModel.items }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { data ->
                    val (change, nodes) = data
                    val model = component.monitorStateTree.model as DefaultTreeModel
                    val rootNode = model.root as DefaultMutableTreeNode
                    if (change == null) {
                        //reload
                        rootNode.removeAllChildren()
                        nodes.forEachIndexed { index, item -> rootNode.add(item.makeNode(index, if (index == 0) null else nodes[index - 1].time)) }
                        model.reload()
                    } else {
                        //update with change
                        when (change) {
                            is ChangeOperation.Insert -> {
                                val newNode = nodes[change.index].makeNode(change.index, if (change.index == 0) null else nodes[change.index - 1].time)
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

private fun Payload.makeNode(index: Int = -1, referenceDate: Date?): DefaultMutableTreeNode {
    val orderString = if (index == -1) "" else "[$index]"
    val action = action
    val timeStamp = time

    val shownTime = if (referenceDate == null) {
        SimpleDateFormat("hh:mm:ss.SSS", Locale.getDefault()).format(time)
    } else {
        val diff = TimeUnit.MILLISECONDS.convert(timeStamp.time - referenceDate.time, TimeUnit.MILLISECONDS)

        val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
        val secondInMillis = TimeUnit.SECONDS.toMillis(1)
        when {
            // minutes
            diff > minuteInMillis -> "+ ${diff / (minuteInMillis)} Mins"
            // seconds
            diff > secondInMillis -> "+ ${diff / secondInMillis}.${diff % secondInMillis} Secs"
            // millis
            else -> "+ $diff Millis"
        }
    }

    return DefaultMutableTreeNode("$orderString $action - $shownTime").apply {
        val leaf = DefaultMutableTreeNode(state)
        add(leaf)
    }
}
