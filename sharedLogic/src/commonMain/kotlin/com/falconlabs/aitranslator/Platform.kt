package com.falconlabs.aitranslator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform