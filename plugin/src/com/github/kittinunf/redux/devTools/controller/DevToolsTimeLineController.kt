package com.github.kittinunf.redux.devTools.controller

import com.github.kittinunf.redux.devTools.server.Server
import com.github.kittinunf.redux.devTools.ui.*
import com.github.kittinunf.redux.devTools.util.addTo
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsTimeLineActionState
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsTimeLineViewModel
import com.github.kittinunf.redux.devTools.viewmodel.DevToolsTimeLineViewModelCommand
import jiconfont.icons.FontAwesome
import jiconfont.swing.IconFontSwing
import rx.Observable
import rx.schedulers.SwingScheduler
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit
import javax.swing.JSlider

/**
 * Created by kittinunf on 8/19/16.
 */

class DevToolsTimeLineController(component: DevToolsPanelComponent) {

    private val viewModels: Observable<DevToolsTimeLineViewModel>

    val timeLineValues by lazy {
        component.timeSliderValueDidChanged()
                .map { (it.source as JSlider).value }
                .distinctUntilChanged()
    }

    private val subscriptionBag = CompositeSubscription()

    init {
        val forwardCommand = component.forwardButtonDidPressed().map { DevToolsTimeLineViewModelCommand.Forward() }
        val backwardCommand = component.backwardButtonDidPressed().map { DevToolsTimeLineViewModelCommand.Backward() }
        val playOrPauseCommand = component.actionButtonDidPressed().map { DevToolsTimeLineViewModelCommand.PlayOrPause() }
        val setValueCommand = component.timeSliderValueDidChanged().map { DevToolsTimeLineViewModelCommand.SetToValue((it.source as JSlider).value) }
        val adjustMaxAndSendToMaxCommand = Server.inputs.concatMap {
            Observable.from(listOf(DevToolsTimeLineViewModelCommand.AdjustMax(), DevToolsTimeLineViewModelCommand.SetToMax()))
        }

        viewModels = Observable.merge(forwardCommand, backwardCommand, playOrPauseCommand, setValueCommand, adjustMaxAndSendToMaxCommand)
                .scan(DevToolsTimeLineViewModel(maxValue = 0)) { viewModel, command ->
                    viewModel.executeCommand(command)
                }
                .replay(1)
                .autoConnect()

        component.actionButtonDidPressed()
                .withLatestFrom(viewModels.map { it.state }) { actionEvent, state -> state }
                //from pause state
                .filter { it == DevToolsTimeLineActionState.PAUSE }
                .subscribe {
                    Observable.fromCallable { component.timeLineForwardButton.doClick() }
                            .subscribeOn(SwingScheduler.getInstance())
                            .repeatWhen { it.delay(800, TimeUnit.MILLISECONDS) }
                            .takeUntil(component.actionButtonDidPressed())
                            .publish()
                            .connect()
                }
                .addTo(subscriptionBag)

        //play or pause
        viewModels.map { if (it.state == DevToolsTimeLineActionState.PLAY) FontAwesome.PAUSE else FontAwesome.PLAY }
                .observeOn(SwingScheduler.getInstance())
                .subscribe {
                    component.timeLineActionButton.icon = IconFontSwing.buildIcon(it, 18.0f)
                }
                .addTo(subscriptionBag)

        //timeline slider
        viewModels.map { (it.value to it.maxValue) }
                .observeOn(SwingScheduler.getInstance())
                .subscribe {
                    component.timeLineTimeSlider.model.valueIsAdjusting = true
                    component.timeLineTimeSlider.maximum = it.second
                    component.timeLineTimeSlider.value = it.first
                }
                .addTo(subscriptionBag)

        //backward button
        viewModels.map { it.backwardEnabled }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { component.timeLineBackwardButton.isEnabled = it }
                .addTo(subscriptionBag)

        //forward button
        viewModels.map { it.forwardEnabled }
                .observeOn(SwingScheduler.getInstance())
                .subscribe { component.timeLineForwardButton.isEnabled = it }
                .addTo(subscriptionBag)
    }

}