package com.github.kittinunf.redux.devTools.ui

import rx.Observable
import rx.observables.SwingObservable
import java.awt.event.ActionEvent
import java.beans.PropertyChangeEvent
import javax.swing.JSlider
import javax.swing.event.ChangeEvent

/**
 * Created by kittinunf on 8/19/16.
 */

fun DevToolsPanelComponent.actionButtonDidPressed(): Observable<ActionEvent> = SwingObservable.fromButtonAction(timeLineActionButton)

fun DevToolsPanelComponent.timeSliderValueDidChanged(): Observable<ChangeEvent> =
        SwingObservable.fromChangeEvents(timeLineTimeSlider)
                .filter { !(it.source as JSlider).valueIsAdjusting }

fun DevToolsPanelComponent.backwardButtonDidPressed(): Observable<ActionEvent> = SwingObservable.fromButtonAction(timeLineBackwardButton)

fun DevToolsPanelComponent.backwardButtonEnabledDidChanged(): Observable<PropertyChangeEvent> = SwingObservable.fromPropertyChangeEvents(timeLineBackwardButton, "enabled")

fun DevToolsPanelComponent.forwardButtonDidPressed(): Observable<ActionEvent> = SwingObservable.fromButtonAction(timeLineForwardButton)

fun DevToolsPanelComponent.forwardButtonEnabledDidChanged(): Observable<PropertyChangeEvent> = SwingObservable.fromPropertyChangeEvents(timeLineForwardButton, "enabled")

