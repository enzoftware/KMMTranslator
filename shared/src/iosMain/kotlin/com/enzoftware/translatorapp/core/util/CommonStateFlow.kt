package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
actual open class CommonStateFlow<T> actual constructor(
    private val flow: StateFlow<T>,
) : CommonFlow<T>(flow), StateFlow<T> by flow {

    actual override val value: T
        get() = flow.value

    actual override val replayCache: List<T>
        get() = flow.replayCache

    actual override suspend fun collect(collector: FlowCollector<T>): Nothing {
        flow.collect(collector)
    }
}