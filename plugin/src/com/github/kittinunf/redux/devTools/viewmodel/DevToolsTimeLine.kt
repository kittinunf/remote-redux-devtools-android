package com.github.kittinunf.redux.devTools.viewmodel

/**
 * Created by kittinunf on 8/16/16.
 */


sealed class DevToolsTimeLineViewModelCommand {

    class Reset(val maxValue: Int) : DevToolsTimeLineViewModelCommand()
    class Forward : DevToolsTimeLineViewModelCommand()
    class Backward : DevToolsTimeLineViewModelCommand()
    class PlayOrPause : DevToolsTimeLineViewModelCommand()
    class SetToValue(val value: Int) : DevToolsTimeLineViewModelCommand()
    class AdjustMax : DevToolsTimeLineViewModelCommand()
    class SetToMax() : DevToolsTimeLineViewModelCommand()

}

enum class DevToolsTimeLineActionState {
    PLAY,
    PAUSE
}

data class DevToolsTimeLineViewModel(val state: DevToolsTimeLineActionState = DevToolsTimeLineActionState.PAUSE,
                                     val value: Int = 0,
                                     val maxValue: Int,
                                     val backwardEnabled: Boolean = false,
                                     val forwardEnabled: Boolean = false) {

    fun executeCommand(command: DevToolsTimeLineViewModelCommand): DevToolsTimeLineViewModel {
        when (command) {
            is DevToolsTimeLineViewModelCommand.Reset -> {
                return DevToolsTimeLineViewModel(maxValue = command.maxValue)
            }

            is DevToolsTimeLineViewModelCommand.Forward -> {
                val shiftedValue = value + 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue > maxValue) value else shiftedValue
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.Backward -> {
                val shiftedValue = value - 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue < 0) value else shiftedValue
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.PlayOrPause -> {
                val newState = if (state == DevToolsTimeLineActionState.PLAY) DevToolsTimeLineActionState.PAUSE else DevToolsTimeLineActionState.PLAY
                return this.copy(state = newState)
            }

            is DevToolsTimeLineViewModelCommand.SetToValue -> {
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(command.value)
                return this.copy(value = command.value, forwardEnabled = newForwardEnabled, backwardEnabled = newBackwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.AdjustMax -> {
                val newMax = maxValue + 1
                return this.copy(maxValue = newMax)
            }

            is DevToolsTimeLineViewModelCommand.SetToMax -> {
                val newValue = maxValue
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(newValue)
                return this.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled)
            }
        }
    }

    private fun validateBoundary(value: Int): Pair<Boolean, Boolean> {
        return (value != 0) to (value != maxValue)
    }

}

