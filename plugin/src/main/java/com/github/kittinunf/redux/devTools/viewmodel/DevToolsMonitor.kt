package com.github.kittinunf.redux.devTools.viewmodel

import com.github.kittinunf.redux.devTools.Payload

sealed class DevToolsMonitorAction {

    class SetItem(val items: List<Payload> = listOf()) : DevToolsMonitorAction()
    class AddItem(val item: Payload) : DevToolsMonitorAction()
    class ShiftItem(val item: Payload) : DevToolsMonitorAction()
}

sealed class ChangeOperation {

    abstract val index: Int

    class Insert(override val index: Int) : ChangeOperation()
    class Update(override val index: Int) : ChangeOperation()
    class Remove(override val index: Int) : ChangeOperation()
}

data class DevToolsMonitorState(val change: ChangeOperation? = null, val items: List<Payload> = listOf()) {
    companion object {
        fun reduce(currentState: DevToolsMonitorState, action: DevToolsMonitorAction) = when (action) {
            is DevToolsMonitorAction.SetItem -> {
                DevToolsMonitorState(null, action.items)
            }

            is DevToolsMonitorAction.AddItem -> {
                val newItems = currentState.items.toMutableList()
                newItems.add(action.item)
                DevToolsMonitorState(ChangeOperation.Insert(newItems.lastIndex), newItems)
            }

            is DevToolsMonitorAction.ShiftItem -> {
                val newItems = currentState.items.toMutableList()
                newItems.removeAt(0)
                newItems.add(action.item)
                DevToolsMonitorState(null, newItems)
            }
        }
    }
}
