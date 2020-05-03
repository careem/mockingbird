package com.careem.mockingbird.test

actual fun <T> threadedTest(body: () -> T): T = body()