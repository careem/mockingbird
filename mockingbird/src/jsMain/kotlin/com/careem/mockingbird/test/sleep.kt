package com.careem.mockingbird.test

import kotlin.js.Date

actual fun sleep(millis: Long) { // TODO find a better way for this that doesn't keep thread busy
    val end = Date.now().toLong() + millis
    while (Date.now().toLong() < end) {
    }
}