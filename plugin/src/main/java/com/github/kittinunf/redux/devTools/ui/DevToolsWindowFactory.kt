package com.github.kittinunf.redux.devTools.ui

import com.github.kittinunf.redux.devTools.controller.DevToolsMonitorController
import com.github.kittinunf.redux.devTools.controller.DevToolsStatusController
import com.github.kittinunf.redux.devTools.controller.DevToolsTimeLineController
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DevToolsWindowFactory : ToolWindowFactory {

    private val component: DevToolsPanelComponent = DevToolsPanelComponent()

    private val statusController: DevToolsStatusController
    private val monitorController: DevToolsMonitorController
    private val timeLineController: DevToolsTimeLineController

    init {
        //base
        statusController = DevToolsStatusController(component)
        monitorController = DevToolsMonitorController(component)
        timeLineController = DevToolsTimeLineController(component)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(component.devToolsPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

}
