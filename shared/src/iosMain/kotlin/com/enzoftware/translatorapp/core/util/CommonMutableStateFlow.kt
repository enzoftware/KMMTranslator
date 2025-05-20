package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
actual open class CommonMutableStateFlow<T> actual constructor(
    private val flow: MutableStateFlow<T>
) : CommonStateFlow<T>(flow), MutableStateFlow<T> by flow {

    override val subscriptionCount: StateFlow<Int>
        get() = flow.subscriptionCount

    override fun compareAndSet(expect: T, update: T): Boolean {
        return flow.compareAndSet(expect, update)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetReplayCache() {
        flow.resetReplayCache()
    }

    override fun tryEmit(value: T): Boolean {
        return flow.tryEmit(value)
    }

    override suspend fun emit(value: T) {
        return flow.emit(value)
    }

    override var value: T
        get() = super.value
        set(value) {
            flow.value = value
        }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        return flow.collect(collector)
    }

    override val replayCache: List<T>
        get() = flow.replayCache

}