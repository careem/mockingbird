package com.careem.mockingbird.test

import co.touchlab.stately.isolate.IsolateState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SharedImmutable
internal val invocationRecorder = IsolateState { InvocationRecorder() }

interface Mock

fun <T> T.verify(exactly: Int = 1, methodName: String, arguments: Map<String, Any?> = emptyMap()) {
    invocationRecorder.access { recorder ->
        assertTrue(this is Mock, "You can't verify a non mock object")
        val methodInvocations = recorder.getInvocations(this)
            .filter { it.methodName == methodName }
        val argumentsInvocations = methodInvocations
            .filter { compareArguments(it.arguments, arguments) }

        assertEquals(
            exactly,
            argumentsInvocations.size,
            """
            Expected $exactly invocations but found ${argumentsInvocations.size} that match the provided arguments
            expected: ${Invocation(methodName = methodName, arguments = arguments)}        
            found: $methodInvocations
        """.trimIndent()
        )
    }
}

fun <T, R> T.every(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    returns: () -> R
) {
    invocationRecorder.access { recorder ->
        assertTrue(this is Mock, "You can't mock responses for a non mock")
        recorder.storeResponse(
            this,
            Invocation(methodName = methodName, arguments = arguments),
            returns()
        )
    }
}

fun <T, R> T.everyAnswers(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    answer: (Invocation) -> R
) {
    invocationRecorder.access { recorder ->
        assertTrue(this is Mock, "You can't mock responses for a non mock")
        recorder.storeAnswer(
            this,
            Invocation(methodName = methodName, arguments = arguments),
            answer
        )
    }
}

fun <T, R> T.mock(methodName: String, arguments: Map<String, Any?> = emptyMap()): R =
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        mockInternal(recorder, invocation)
        return@access recorder.getResponse(this as Any, invocation) as R
    }


/**
 * Convinient function to mock a function
 * @param methodName name of the method that you want mock
 * @param arguments names of the function that you want to mock
 * @param relaxed specify if we want to crash if no mock behavior is provided for the function (relaxed=false => crash)
 */
fun <T> T.mockUnit(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    relaxed: Boolean = true
) {
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        mockInternal(recorder, invocation)
        recorder.getResponse(
            instance = this as Any,
            invocation = invocation,
            relaxed = relaxed
        )
    }
}

/**
 * Capture any [Slot] which will be using to compare the property inside
 */
fun <T> capture(slot: Slot<T>): Captured<T> {
    return Captured(slot)
}

/**
 * A any() matcher which will matching any object
 */
fun any(): AnyMatcher {
    return AnyMatcher()
}

/**
 * A placeholder for where using any() as a testing matcher
 */
class AnyMatcher

/**
 * A slot using to fetch the method invocation and compare the property inside invocation arguments
 * Usage example @see [FunctionsTest]
 */
class Slot<T> {
    private val _captured: AtomicRef<T?> = atomic(null)
    var captured: T?
        get() {
            return _captured.value
        }
        set(value) {
            _captured.value = value
        }
}

/**
 * A class to indicate this argument is captured by [Slot]
 * Usage example @see [FunctionsTest]
 */
class Captured<T>(private val slot: Slot<T>) {
    fun setCapturedValue(value: Any?) {
        slot.captured = value as T
    }
}

private fun compareArguments(
    invocationArguments: Map<String, Any?>,
    expectedArguments: Map<String, Any?>
): Boolean {
    for ((arg, value) in expectedArguments) {
        if (value is Captured<*> && arg in invocationArguments) {
            value.setCapturedValue(invocationArguments[arg])
        } else if (value is AnyMatcher) {
            continue
        } else if (!invocationArguments.containsKey(arg) ||
            invocationArguments[arg] != expectedArguments[arg]
        ) {
            return false
        }
    }
    return true
}

private fun <T> T.mockInternal(recorder: InvocationRecorder, invocation: Invocation) {
    assertTrue(this is Mock, "You can't mock a non Mock object")
    recorder.storeInvocation(
        instance = this,
        invocation = invocation
    )
}