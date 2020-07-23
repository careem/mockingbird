package com.careem.mockingbird.test

actual fun sleep(millis: Long) = Thread.sleep(millis)