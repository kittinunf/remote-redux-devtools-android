package com.github.kittinunf.redux.devTools.viewmodel

import com.github.kittinunf.redux.devTools.InstrumentAction

sealed class DevToolsMonitorViewModelCommand {

    class SetItem(val items: List<InstrumentAction.SetState> = listOf()) : DevToolsMonitorViewModelCommand()
    class AddItem(val item: InstrumentAction.SetState) : DevToolsMonitorViewModelCommand()
    class ShiftItem(val item: InstrumentAction.SetState) : DevToolsMonitorViewModelCommand()
}

sealed class ChangeOperation {

    abstract val index: Int

    class Insert(override val index: Int) : ChangeOperation()
    class Update(override val index: Int) : ChangeOperation()
    class Remove(override val index: Int) : ChangeOperation()

}

data class DevToolsMonitorViewModel(val change: ChangeOperation? = null, val items: List<InstrumentAction.SetState> = listOf()) {

    fun executeCommand(command: DevToolsMonitorViewModelCommand): DevToolsMonitorViewModel {
        when (command) {
            is DevToolsMonitorViewModelCommand.SetItem -> {
                return DevToolsMonitorViewModel(null, command.items)
            }

            is DevToolsMonitorViewModelCommand.AddItem -> {
                val newItems = items.toMutableList()
                newItems.add(command.item)
                return DevToolsMonitorViewModel(ChangeOperation.Insert(newItems.lastIndex), newItems)
            }

            is DevToolsMonitorViewModelCommand.ShiftItem -> {
                val newItems = items.toMutableList()
                newItems.removeAt(0)
                newItems.add(command.item)
                return DevToolsMonitorViewModel(null, newItems)
            }
        }
    }
}


