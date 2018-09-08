package com.github.kittinunf.redux.devTools.viewmodel

import com.github.kittinunf.redux.devTools.InstrumentAction

sealed class DevToolsMonitorAction {

    class SetItem(val items: List<InstrumentAction.SetState> = listOf()) : DevToolsMonitorAction()
    class AddItem(val item: InstrumentAction.SetState) : DevToolsMonitorAction()
    class ShiftItem(val item: InstrumentAction.SetState) : DevToolsMonitorAction()
}

sealed class ChangeOperation {

    abstract val index: Int

    class Insert(override val index: Int) : ChangeOperation()
    class Update(override val index: Int) : ChangeOperation()
    class Remove(override val index: Int) : ChangeOperation()
}

data class DevToolsMonitorState(val change: ChangeOperation? = null, val items: List<InstrumentAction.SetState> = listOf()) {

    fun executeCommand(command: DevToolsMonitorAction): DevToolsMonitorState {
        when (command) {
            is DevToolsMonitorAction.SetItem -> {
                return DevToolsMonitorState(null, command.items)
            }

            is DevToolsMonitorAction.AddItem -> {
                val newItems = items.toMutableList()
                newItems.add(command.item)
                return DevToolsMonitorState(ChangeOperation.Insert(newItems.lastIndex), newItems)
            }

            is DevToolsMonitorAction.ShiftItem -> {
                val newItems = items.toMutableList()
                newItems.removeAt(0)
                newItems.add(command.item)
                return DevToolsMonitorState(null, newItems)
            }
        }
    }
}


