package com.github.kittinunf.redux.devTools.viewmodel

sealed class DevToolsStatusViewModelCommand {

    class SetAddress(val address: String) : DevToolsStatusViewModelCommand()
    class SetClient(val status: String) : DevToolsStatusViewModelCommand()
}

data class DevToolsStatusViewModel(val address: String = "", val status: String = "-") {

    fun executeCommand(command: DevToolsStatusViewModelCommand): DevToolsStatusViewModel {
        return when (command) {
            is DevToolsStatusViewModelCommand.SetAddress -> {
                this.copy(address = command.address)
            }

            is DevToolsStatusViewModelCommand.SetClient -> {
                this.copy(status = command.status)
            }
        }
    }

}
