/**
 *
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.careem.mockingbird.test

import co.touchlab.stately.isolate.IsolateState
import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.assertEquals

@SharedImmutable
private val invocationRecorder = IsolateState { InvocationRecorder() }

@SharedImmutable
internal const val AWAIT_POOLING_TIME = 10L

public interface Mock
public interface Spy : Mock

/**
 * Function to specify the return value of an invocation
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 */
public fun <T : Mock, R> T.every(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    returns: () -> R
) {
    val hashCode = this.hashCode()
    val value = returns()
    invocationRecorder.access { recorder ->
        recorder.storeResponse(
            hashCode,
            Invocation(methodName = methodName, arguments = arguments),
            value
        )
    }
}

/**
 * Function to specify the behavior when an invocation is called
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 */
public fun <T : Mock, R> T.everyAnswers(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    answer: (Invocation) -> R
) {
    val hashCode = this.hashCode()
    invocationRecorder.access { recorder ->
        recorder.storeAnswer(
            hashCode,
            Invocation(methodName = methodName, arguments = arguments),
            answer
        )
    }
}

/**
 * Function to verify invocation is invoked with specific arguments
 * @param exactly number of times invocation is invoked
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 * @param timeoutMillis milliseconds allowed to wait until the condition is considered false
 */
public fun <T : Mock> T.verify(
    exactly: Int = 1,
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    timeoutMillis: Long = 0L
) {
    val elapsedTime = atomic(0L)
    val run = atomic(true)
    while (run.value && elapsedTime.value < timeoutMillis) {
        try {
            this.rawVerify(
                exactly = exactly,
                methodName = methodName,
                arguments = arguments
            )
            run.value = false
        } catch (e: AssertionError) {
            sleep(AWAIT_POOLING_TIME)
            elapsedTime.value += AWAIT_POOLING_TIME
        }
    }
    this.rawVerify(
        exactly = exactly,
        methodName = methodName,
        arguments = arguments
    )
}

internal fun <T : Mock> T.rawVerify(
    exactly: Int,
    methodName: String,
    arguments: Map<String, Any?>
) {
    val hashCode = this.hashCode()
    invocationRecorder.access { recorder ->
        val methodInvocations = recorder.getInvocations(hashCode)
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

/**
 * Function to mock a function
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 * @return returns the mocked result for the method call described by arguments above ( it crash if no mock behavior provided )
 */
public fun <T : Mock, R> T.mock(methodName: String, arguments: Map<String, Any?> = emptyMap()): R {
    val hashCode = this.hashCode()
    return invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(hashCode, recorder, invocation)
        return@access recorder.getResponse(hashCode, invocation) as R
    }
}


/**
 * Convenient function to mock a unit function
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 * @param relaxed specify if we want to crash if no mock behavior is provided for the function (relaxed=false => crash)
 */
public fun <T : Mock> T.mockUnit(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    relaxed: Boolean = true
) {
    val hashCode = this.hashCode()
    invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(hashCode, recorder, invocation)
        recorder.getResponse(
            instanceHash = hashCode,
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
public fun <T : Spy, R> T.spy(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    delegate: () -> R
): R {
    val hashCode = this.hashCode()
    return invocationRecorder.access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(hashCode, recorder, invocation)// TODO change name
        val mockResponse = recorder.getResponse(
            instanceHash = hashCode,
            invocation = invocation,
            relaxed = true
        ) as R
        return@access mockResponse ?: delegate()
    }
}

/**
 * A any() matcher which will matching any object
 */
public fun any(): AnyMatcher {
    return AnyMatcher()
}

private fun compareArguments(
    invocationArguments: Map<String, Any?>,
    expectedArguments: Map<String, Any?>
): Boolean {
    if (invocationArguments.size != expectedArguments.size) {
        return false
    }
    for ((arg, value) in expectedArguments) {
        if (value is CapturedMatcher<*> && arg in invocationArguments) {
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

private fun recordInvocation(instanceHash: Int, recorder: InvocationRecorder, invocation: Invocation) {
    recorder.storeInvocation(
        instanceHash = instanceHash,
        invocation = invocation
    )
}