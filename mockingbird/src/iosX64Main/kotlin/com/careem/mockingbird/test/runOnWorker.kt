package com.careem.mockingbird.test

import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual fun <T> runOnWorker(body: () -> T): T {
    val worker = Worker.start()
    body.freeze()
    val future = worker.execute(TransferMode.SAFE, { body }) {
        println("Running body in worker")
        runCatching(it)
    }
    return future.result.getOrThrow()
}