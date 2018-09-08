package com.github.kittinunf.redux.devTools.viewmodel

sealed class DevToolsStatusAction {

    class SetAddress(val address: String) : DevToolsStatusAction()
    class SetClient(val status: String) : DevToolsStatusAction()
}

data class DevToolsStatusState(val address: String = "", val status: String = "-") {

    fun executeCommand(command: DevToolsStatusAction): DevToolsStatusState {
        return when (command) {
            is DevToolsStatusAction.SetAddress -> {
                this.copy(address = command.address)
            }

            is DevToolsStatusAction.SetClient -> {
                this.copy(status = command.status)
            }
        }
    }

}
