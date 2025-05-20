package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

expect class CommonStateFlow<T>(flow: StateFlow<T>)

fun <T> StateFlow<T>.toCommonStateFlow() = CommonStateFlow(this)