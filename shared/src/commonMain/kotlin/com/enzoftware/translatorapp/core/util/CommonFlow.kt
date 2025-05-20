package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.flow.Flow

expect class CommonFlow<T>(flow: Flow<T>)

fun <T> Flow<T>.toCommonFlow() = CommonFlow(this)
