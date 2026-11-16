package com.enzoftware.translatorapp.translate.domain.translate

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.core.util.CommonFlow
import com.enzoftware.translatorapp.core.util.Resource
import com.enzoftware.translatorapp.core.util.toCommonFlow
import com.enzoftware.translatorapp.translate.data.remote.FakeTranslateClient
import com.enzoftware.translatorapp.translate.domain.history.HistoryDataSource
import com.enzoftware.translatorapp.translate.domain.history.HistoryItem
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test

class TranslateUseCaseTest {

    private class FailingHistoryDataSource : HistoryDataSource {
        override fun getHistory(coroutineContext: CoroutineContext): CommonFlow<List<HistoryItem>> =
            emptyFlow<List<HistoryItem>>().toCommonFlow()

        override suspend fun insertHistoryItem(item: HistoryItem) = error("history unavailable")
    }

    @Test
    fun historyFailure_stillReturnsTheTranslation() = runTest {
        val client = FakeTranslateClient().apply { translatedText = "Hola" }
        val useCase = TranslateUseCase(client, FailingHistoryDataSource())

        val result = useCase.execute(Language.ENGLISH, "Hello", Language.SPANISH)

        assertThat(result).isInstanceOf<Resource.Success<String>>()
        assertThat(result.data).isEqualTo("Hola")
    }
}
