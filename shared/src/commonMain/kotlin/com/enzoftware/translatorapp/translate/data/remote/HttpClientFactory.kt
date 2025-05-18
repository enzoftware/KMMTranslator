package com.enzoftware.translatorapp.translate.data.remote

import io.ktor.client.HttpClient

expect class HttpClientFactory {
    expect fun create(): HttpClient

}