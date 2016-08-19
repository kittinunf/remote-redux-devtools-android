package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.client.Client
import com.github.kittinunf.redux.devTools.server.Server
import com.github.kittinunf.redux.devTools.ui.DevToolsPanelComponent
import com.github.kittinunf.redux.devTools.util.addTo
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import rx.Observable
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

/**
 * Created by kittinunf on 8/16/16.
 */

class DevToolsWindowFactory : ToolWindowFactory {

    val component: DevToolsPanelComponent

    val monitorController: DevToolsMonitorController
    val timeLineController: DevToolsTimeLineController

    val subscriptionBag = CompositeSubscription()

    init {
        component = DevToolsPanelComponent()
        monitorController = DevToolsMonitorController(component)
        timeLineController = DevToolsTimeLineController(component)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(component.devToolsPanel, "", false)
        toolWindow.contentManager.addContent(content)

        Server.start()
        Client.connect().addTo(subscriptionBag)

        Observable.interval(2, TimeUnit.SECONDS)
                .take(3)
                .subscribe {
                    val text = "{ counter : $it }"
                    Client.send(text)
                }
                .addTo(subscriptionBag)

        timeLineController.timeLineValues.subscribe { println(it) }.addTo(subscriptionBag)
    }

}