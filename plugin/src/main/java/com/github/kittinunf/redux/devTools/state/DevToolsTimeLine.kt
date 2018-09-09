package com.github.kittinunf.redux.devTools.state

sealed class DevToolsTimeLineAction {

    class Reset(val maxValue: Int) : DevToolsTimeLineAction()
    object Forward : DevToolsTimeLineAction()
    object Backward : DevToolsTimeLineAction()
    object PlayOrPause : DevToolsTimeLineAction()
    class SetToValue(val value: Int) : DevToolsTimeLineAction()
    object AdjustMax : DevToolsTimeLineAction()
    object SetToMax : DevToolsTimeLineAction()
}

data class DevToolsTimeLineState(val initialState: Boolean = true,
                                 val playState: PlayState = PlayState.PAUSE,
                                 val value: Int = 0,
                                 val maxValue: Int,
                                 val backwardEnabled: Boolean = false,
                                 val forwardEnabled: Boolean = false,
                                 val shouldNotifyClient: Boolean = false) {

    enum class PlayState {
        PLAY,
        PAUSE
    }

    companion object {
        fun reduce(currentState: DevToolsTimeLineState, action: DevToolsTimeLineAction) = when (action) {

            is DevToolsTimeLineAction.Reset -> {
                DevToolsTimeLineState(initialState = true, maxValue = action.maxValue, shouldNotifyClient = false)
            }

            is DevToolsTimeLineAction.Forward -> {
                val shiftedValue = currentState.value + 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue, currentState.maxValue)
                val newValue = if (shiftedValue > currentState.maxValue) currentState.value else shiftedValue
                currentState.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.Backward -> {
                val shiftedValue = currentState.value - 1
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(shiftedValue, currentState.maxValue)
                val newValue = if (shiftedValue < 0) currentState.value else shiftedValue
                currentState.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.PlayOrPause -> {
                val newState = if (currentState.playState == PlayState.PLAY)
                    PlayState.PAUSE else PlayState.PLAY
                currentState.copy(playState = newState, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.SetToValue -> {
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(action.value, currentState.maxValue)
                currentState.copy(value = action.value, forwardEnabled = newForwardEnabled, backwardEnabled = newBackwardEnabled, shouldNotifyClient = true)
            }

            is DevToolsTimeLineAction.AdjustMax -> {
                val newMax = if (currentState.initialState) currentState.maxValue else currentState.maxValue + 1
                currentState.copy(initialState = false, maxValue = newMax, shouldNotifyClient = false)
            }

            is DevToolsTimeLineAction.SetToMax -> {
                val newValue = currentState.maxValue
                val (newBackwardEnabled, newForwardEnabled) = validateBoundary(newValue, currentState.maxValue)
                currentState.copy(value = newValue, backwardEnabled = newBackwardEnabled, forwardEnabled = newForwardEnabled, shouldNotifyClient = false)
            }
        }

        private fun validateBoundary(value: Int, maxValue: Int): Pair<Boolean, Boolean> {
            return (value != 0) to (value != maxValue)
        }
    }
}

