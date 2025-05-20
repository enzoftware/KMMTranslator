package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.flow.MutableStateFlow

expect class CommonMutableStateFlow<T>(flow: MutableStateFlow<T>)

fun <T> MutableStateFlow<T>.toCommonMutableStateFlow() = CommonMutableStateFlow(this)