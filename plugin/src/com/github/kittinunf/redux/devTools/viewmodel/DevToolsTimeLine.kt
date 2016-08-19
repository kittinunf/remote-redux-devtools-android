package com.github.kittinunf.redux.devTools.viewmodel

/**
 * Created by kittinunf on 8/16/16.
 */


sealed class DevToolsTimeLineViewModelCommand {

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
            is DevToolsTimeLineViewModelCommand.Forward -> {
                val shiftedValue = value + 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue > maxValue) value else shiftedValue
                return DevToolsTimeLineViewModel(state, newValue, maxValue, newBackwardEnabled, newForwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.Backward -> {
                val shiftedValue = value - 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue < 0) value else shiftedValue
                return DevToolsTimeLineViewModel(state, newValue, maxValue, newBackwardEnabled, newForwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.PlayOrPause -> {
                val newState = if (state == DevToolsTimeLineActionState.PLAY) DevToolsTimeLineActionState.PAUSE else DevToolsTimeLineActionState.PLAY
                return DevToolsTimeLineViewModel(newState, value, maxValue, backwardEnabled, forwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.SetToValue -> {
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(command.value)
                return DevToolsTimeLineViewModel(state, command.value, maxValue, newBackwardEnabled, newForwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.AdjustMax -> {
                val newMax = maxValue + 1
                return DevToolsTimeLineViewModel(state, value, newMax, backwardEnabled, forwardEnabled)
            }

            is DevToolsTimeLineViewModelCommand.SetToMax -> {
                val newValue = maxValue
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(newValue)
                return DevToolsTimeLineViewModel(state, newValue, maxValue, newBackwardEnabled, newForwardEnabled)
            }
        }
    }

    private fun validateBoundary(value: Int): Pair<Boolean, Boolean> {
        return (value != 0) to (value != maxValue)
    }

}

