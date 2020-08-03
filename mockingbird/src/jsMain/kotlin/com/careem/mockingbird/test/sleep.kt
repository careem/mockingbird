package com.careem.mockingbird.test

import com.careem.mockingbird.util.getSystemTimeInMillis

actual fun sleep(millis: Long) { // TODO find a better way for this that doesn't keep thread busy
    val end = getSystemTimeInMillis() + millis
    while (getSystemTimeInMillis() < end) {
    }
}