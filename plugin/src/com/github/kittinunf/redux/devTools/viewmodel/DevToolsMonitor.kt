package com.github.kittinunf.redux.devTools.viewmodel

import javax.swing.tree.DefaultMutableTreeNode

/**
 * Created by kittinunf on 8/18/16.
 */

data class Entry(val actionName: String,
                 val state: String) {

    fun makeNode(): DefaultMutableTreeNode {
        return DefaultMutableTreeNode(actionName).apply {
            val leaf = DefaultMutableTreeNode(state)
            add(leaf)
        }
    }

}

sealed class DevToolsMonitorViewModelCommand {

    class AddItem(val item: Entry) : DevToolsMonitorViewModelCommand()

}

sealed class ChangeOperation() {

    abstract val index: Int

    class Insert(override val index: Int) : ChangeOperation()
    class Update(override val index: Int) : ChangeOperation()
    class Remove(override val index: Int) : ChangeOperation()

}

data class DevToolsMonitorViewModel(val change: ChangeOperation? = null, val items: List<Entry> = listOf()) {

    fun executeCommand(command: DevToolsMonitorViewModelCommand): DevToolsMonitorViewModel {
        when (command) {
            is DevToolsMonitorViewModelCommand.AddItem -> {
                val newItems = items.toMutableList()
                newItems.add(command.item)
                return DevToolsMonitorViewModel(ChangeOperation.Insert(newItems.lastIndex), newItems)
            }
        }
    }

}


