package com.github.kittinunf.redux.devTools.viewmodel

sealed class DevToolsStatusAction {

    class SetAddress(val address: String) : DevToolsStatusAction()
    class SetClient(val status: String) : DevToolsStatusAction()
}

data class DevToolsStatusState(val address: String = "", val status: String = "-") {
    companion object {
        fun reduce(currentState: DevToolsStatusState, action: DevToolsStatusAction) = when (action) {
            is DevToolsStatusAction.SetAddress -> {
                currentState.copy(address = action.address)
            }

            is DevToolsStatusAction.SetClient -> {
                currentState.copy(status = action.status)
            }
        }
    }
}
