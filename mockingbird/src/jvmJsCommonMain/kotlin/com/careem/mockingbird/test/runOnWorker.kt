package com.careem.mockingbird.test

actual fun <T> runOnWorker(body: () -> T): T = body()