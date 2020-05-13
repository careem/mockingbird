package com.careem.mockingbird.test

import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual fun <T> threadedTest(body: () -> T): T {
    val worker = Worker.start()
    val future = worker.execute(TransferMode.SAFE, { body.freeze() }) {
        println("Running body in worker")
        runCatching(it)
    }
    return future.result.getOrThrow()
}