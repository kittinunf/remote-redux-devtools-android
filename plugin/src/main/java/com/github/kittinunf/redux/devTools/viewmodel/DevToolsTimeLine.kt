package com.github.kittinunf.redux.devTools.viewmodel

sealed class DevToolsTimeLineViewModelCommand {

    class Reset(val maxValue: Int) : DevToolsTimeLineViewModelCommand()
    class Forward : DevToolsTimeLineViewModelCommand()
    class Backward : DevToolsTimeLineViewModelCommand()
    class PlayOrPause : DevToolsTimeLineViewModelCommand()
    class SetToValue(val value: Int) : DevToolsTimeLineViewModelCommand()
    class AdjustMax : DevToolsTimeLineViewModelCommand()
    class SetToMax : DevToolsTimeLineViewModelCommand()
}

enum class DevToolsTimeLineActionState {
    PLAY,
    PAUSE
}

data class DevToolsTimeLineViewModel(val initialState: Boolean = true,
                                     val state: DevToolsTimeLineActionState = DevToolsTimeLineActionState.PAUSE,
                                     val value: Int = 0,
                                     val maxValue: Int,
                                     val backwardEnabled: Boolean = false,
                                     val forwardEnabled: Boolean = false,
                                     val shouldNotifyClient: Boolean = false) {

    fun executeCommand(command: DevToolsTimeLineViewModelCommand): DevToolsTimeLineViewModel {
        when (command) {
            is DevToolsTimeLineViewModelCommand.Reset -> {
                return DevToolsTimeLineViewModel(initialState = true, maxValue = command.maxValue, shouldNotifyClient = false)
            }

            is DevToolsTimeLineViewModelCommand.Forward -> {
                val shiftedValue = value + 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue > maxValue) value else shiftedValue
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineViewModelCommand.Backward -> {
                val shiftedValue = value - 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue < 0) value else shiftedValue
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineViewModelCommand.PlayOrPause -> {
                val newState = if (state == DevToolsTimeLineActionState.PLAY) DevToolsTimeLineActionState.PAUSE else DevToolsTimeLineActionState.PLAY
                return this.copy(state = newState, shouldNotifyClient = true)
            }

            is DevToolsTimeLineViewModelCommand.SetToValue -> {
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(command.value)
                return this.copy(value = command.value, forwardEnabled = newForwardEnabled, backwardEnabled = newBackwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineViewModelCommand.AdjustMax -> {
                val newMax = if (initialState) maxValue else maxValue + 1
                return this.copy(initialState = false, maxValue = newMax, shouldNotifyClient = false)
            }

            is DevToolsTimeLineViewModelCommand.SetToMax -> {
                val newValue = maxValue
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(newValue)
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = false)
            }
        }
    }

    private fun validateBoundary(value: Int): Pair<Boolean, Boolean> {
        return (value != 0) to (value != maxValue)
    }

}

