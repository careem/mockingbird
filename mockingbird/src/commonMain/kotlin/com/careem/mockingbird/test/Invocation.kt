package com.careem.mockingbird.test

data class Invocation(
    val methodName: String,
    val arguments: Map<String, Any?>
)