package com.careem.mockingbird.test

import co.touchlab.stately.isolate.IsolateState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.assertEquals

@SharedImmutable
internal val invocationRecorder = IsolateState { InvocationRecorder() }

interface Mock
interface Spy : Mock

fun <T : Mock> T.verify(
    exactly: Int = 1,
    methodName: String,
    arguments: Map<String, Any?> = emptyMap()
) {
    invocationRecorder.access { recorder ->
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

fun <T : Mock, R> T.every(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    returns: () -> R
) {
    invocationRecorder.access { recorder ->
        recorder.storeResponse(
            this,
            Invocation(methodName = methodName, arguments = arguments),
            returns()
        )
    }
}

fun <T : Mock, R> T.everyAnswers(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    answer: (Invocation) -> R
) {
    invocationRecorder.access { recorder ->
        recorder.storeAnswer(
            this,
            Invocation(methodName = methodName, arguments = arguments),
            answer
        )
    }
}

/**
 * Convenient function to mock a unit function
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 * @return returns the mocked result for the method call described by arguments above ( it crash if no mock behavior provided )
 */
fun <T : Mock, R> T.mock(methodName: String, arguments: Map<String, Any?> = emptyMap()): R =
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(recorder, invocation)
        return@access recorder.getResponse(this as Any, invocation) as R
    }


/**
 * Convenient function to mock a unit function
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 * @param relaxed specify if we want to crash if no mock behavior is provided for the function (relaxed=false => crash)
 */
fun <T : Mock> T.mockUnit(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    relaxed: Boolean = true
) {
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(recorder, invocation)
        recorder.getResponse(
            instance = this as Any,
            invocation = invocation,
            relaxed = relaxed
        )
    }
}

/**
 * Convenient function to spy methods
 * @param methodName name of the method that you want to spy
 * @param arguments map between names and method arguments
 * @return returns the mocked result for the method call described by arguments above if the method
 * has been mocked, the result of the real invocation otherwise
 */
fun <T : Spy, R> T.spy(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    delegate: () -> R
): R =
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(recorder, invocation)// TODO change name
        val mockResponse = recorder.getResponse(
            instance = this as Any,
            invocation = invocation,
            relaxed = true
        ) as R
        return@access mockResponse ?: delegate()
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

private fun <T : Mock> T.recordInvocation(recorder: InvocationRecorder, invocation: Invocation) {
    recorder.storeInvocation(
        instance = this,
        invocation = invocation
    )
}