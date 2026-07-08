package com.falconlabs.aitranslator

import kotlin.js.JsExport

@JsExport
class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
