package com.enzoftware.translatorapp.translate.data.translate

import com.enzoftware.translatorapp.core.domain.language.Language
import com.enzoftware.translatorapp.translate.domain.translate.TranslateClient
import com.enzoftware.translatorapp.translate.domain.translate.TranslateError
import com.enzoftware.translatorapp.translate.domain.translate.TranslateException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.io.IOException

const val BASE_URL = "https://translate.pl-coding.com"

class KtorTranslateClient(
    private val httpClient: HttpClient
) : TranslateClient {
    override suspend fun translate(
        fromLanguage: Language,
        fromText: String,
        toLanguage: Language
    ): String {
        val result = try {
            httpClient.post {
                url("$BASE_URL/translate")
                contentType(ContentType.Application.Json)
                setBody(
                    TranslateDto(
                        textToTranslate = fromText,
                        sourceLanguageCode = fromLanguage.code,
                        targetLanguageCode = toLanguage.code
                    )
                )
            }
        } catch (e: IOException) {
            throw TranslateException(TranslateError.SERVICE_UNAVAILABLE)
        }

        when (result.status.value) {
            in 200..299 -> Unit
            500 -> throw TranslateException(TranslateError.SERVER_ERROR)
            in 400..499 -> throw TranslateException(TranslateError.CLIENT_ERROR)
            else -> throw TranslateException(TranslateError.UNKNOWN_ERROR)
        }

        return try {
            result.body<TranslatedDto>().translatedText
        } catch (e: Exception) {
            throw TranslateException(TranslateError.SERVER_ERROR)
        }

    }
}