package com.enzoftware.translatorapp.core.util

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
actual class CommonStateFlow<T> actual constructor(
    private val flow: StateFlow<T>
) : StateFlow<T> by flow