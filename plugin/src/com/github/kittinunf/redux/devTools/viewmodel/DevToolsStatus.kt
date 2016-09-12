package com.github.kittinunf.redux.devTools.viewmodel

/**
 * Created by kittinunf on 8/31/16.
 */

sealed class DevToolsStatusViewModelCommand {

    class SetAddress(val address: String) : DevToolsStatusViewModelCommand()
    class SetClient(val status: String) : DevToolsStatusViewModelCommand()

}

data class DevToolsStatusViewModel(val address: String = "", val status: String = "-") {

    fun executeCommand(command: DevToolsStatusViewModelCommand): DevToolsStatusViewModel {
        when (command) {
            is DevToolsStatusViewModelCommand.SetAddress -> {
                return this.copy(address = command.address)
            }

            is DevToolsStatusViewModelCommand.SetClient -> {
                return this.copy(status = command.status)
            }
        }
    }

}
