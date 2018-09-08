package com.github.kittinunf.redux.devTools.viewmodel

sealed class DevToolsTimeLineAction {

    class Reset(val maxValue: Int) : DevToolsTimeLineAction()
    object Forward : DevToolsTimeLineAction()
    object Backward : DevToolsTimeLineAction()
    object PlayOrPause : DevToolsTimeLineAction()
    class SetToValue(val value: Int) : DevToolsTimeLineAction()
    object AdjustMax : DevToolsTimeLineAction()
    object SetToMax : DevToolsTimeLineAction()
}

enum class DevToolsTimeLinePlayState {
    PLAY,
    PAUSE
}

data class DevToolsTimeLineState(val initialState: Boolean = true,
                                 val state: DevToolsTimeLinePlayState = DevToolsTimeLinePlayState.PAUSE,
                                 val value: Int = 0,
                                 val maxValue: Int,
                                 val backwardEnabled: Boolean = false,
                                 val forwardEnabled: Boolean = false,
                                 val shouldNotifyClient: Boolean = false) {

    fun executeCommand(command: DevToolsTimeLineAction): DevToolsTimeLineState {
        when (command) {
            is DevToolsTimeLineAction.Reset -> {
                return DevToolsTimeLineState(initialState = true, maxValue = command.maxValue, shouldNotifyClient = false)
            }

            is DevToolsTimeLineAction.Forward -> {
                val shiftedValue = value + 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue > maxValue) value else shiftedValue
                return copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.Backward -> {
                val shiftedValue = value - 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue)
                val newValue = if (shiftedValue < 0) value else shiftedValue
                return copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.PlayOrPause -> {
                val newState = if (state == DevToolsTimeLinePlayState.PLAY) DevToolsTimeLinePlayState.PAUSE else DevToolsTimeLinePlayState.PLAY
                return copy(state = newState, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.SetToValue -> {
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(command.value)
                return copy(value = command.value, forwardEnabled = newForwardEnabled, backwardEnabled = newBackwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.AdjustMax -> {
                val newMax = if (initialState) maxValue else maxValue + 1
                return copy(initialState = false, maxValue = newMax, shouldNotifyClient = false)
            }

            is DevToolsTimeLineAction.SetToMax -> {
                val newValue = maxValue
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(newValue)
                return copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = false)
            }
        }
    }

    private fun validateBoundary(value: Int): Pair<Boolean, Boolean> {
        return (value != 0) to (value != maxValue)
    }
}

