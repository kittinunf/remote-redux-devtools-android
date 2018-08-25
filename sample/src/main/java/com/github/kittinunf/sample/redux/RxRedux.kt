package com.github.kittinunf.sample.redux

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

interface Action

interface State

interface Reducer<S : State> {

    fun reduce(currentState: S, action: Action): S
}

interface Middleware<S : State> {

    fun performBeforeReducingState(currentState: S, action: Action) {}

    fun performAfterReducingState(action: Action, nextState: S) {}
}

interface StoreType<S : State> {

    val states: Observable<S>

    fun dispatch(action: Action)

    fun dispatch(actions: Observable<out Action>): Disposable

    fun addMiddleware(middleware: Middleware<S>)

    fun removeMiddleware(middleware: Middleware<S>): Boolean
}

class Store<S : State>(
        initialState: S,
        reducer: Reducer<S>,
        defaultScheduler: Scheduler = Schedulers.single()
) : StoreType<S> {

    object NoAction : Action

    private val actionSubject = PublishSubject.create<Action>()

    override val states: Observable<S>

    private val middlewares = mutableListOf<Middleware<S>>()

    init {
        states = actionSubject
                .scan(initialState to NoAction as Action) { (state, _), action ->
                    middlewares.onEach { it.performBeforeReducingState(state, action) }
                    val next = reducer.reduce(state, action)
                    next to action
                }
                .doAfterNext { next ->
                    val (nextState, latestAction) = next
                    middlewares.onEach { it.performAfterReducingState(latestAction, nextState) }
                }
                .map(Pair<S, Action>::first)
                .distinctUntilChanged()
                .subscribeOn(defaultScheduler)
                .replay(1)
                .autoConnect()
    }

    override fun dispatch(action: Action) {
        actionSubject.onNext(action)
    }

    @CheckReturnValue
    override fun dispatch(actions: Observable<out Action>): Disposable = actions.subscribe(actionSubject::onNext)

    override fun addMiddleware(middleware: Middleware<S>) {
        middlewares.add(middleware)
    }

    override fun removeMiddleware(middleware: Middleware<S>) = middlewares.remove(middleware)
}
