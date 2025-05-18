package com.enzoftware.translatorapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform