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

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.assertEquals


@SharedImmutable
internal const val AWAIT_POOLING_TIME = 10L

@SharedImmutable
private val uuidGenerator: AtomicInt = atomic(0)

public interface Mock {
    public val uuid: String
}

public interface Spy : Mock


/**
 * Utility function to execute a specific test in a specific testMode, test mode will be reset at the end of the
 * test allowing you to run a test in a specific mode in isolation.
 * @see README section [Test Mode]
 */
@Deprecated(
    message = NEW_MM_MODEL_DEPRECATION_MESSAGE,
    level = DeprecationLevel.WARNING
)
public fun runWithTestMode(@Suppress("UNUSED_PARAMETER", "DEPRECATION")  testMode: TestMode, testBlock: () -> Unit) {
    testBlock()
}

/**
 * Function to specify the return value of an invocation
 * @param methodName name of the method that you want to mock
 * @param arguments map between names and method arguments
 */
public fun <T, R> T.every(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    returns: () -> R
) {
    if (this !is Mock) throw IllegalArgumentException("This object is not a mock")
    val uuid = this.uuid
    val value = returns()
    MockingBird.invocationRecorder().access { recorder ->
        recorder.storeResponse(
            uuid,
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
public fun <T, R> T.everyAnswers(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    answer: (Invocation) -> R
) {
    if (this !is Mock) throw IllegalArgumentException("This object is not a mock")
    val uuid = this.uuid
    MockingBird.invocationRecorder().access { recorder ->
        recorder.storeAnswer(
            uuid,
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
public fun <T> T.verify(
    exactly: Int = 1,
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    timeoutMillis: Long = 0L
) {
    if (this !is Mock) throw IllegalArgumentException("This object is not a mock")
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
    val uuid = this.uuid
    MockingBird.invocationRecorder().access { recorder ->
        val methodInvocations = recorder.getInvocations(uuid)
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
    val uuid = this.uuid
    return MockingBird.invocationRecorder().access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(uuid, recorder, invocation)
        @Suppress("UNCHECKED_CAST")
        recorder.getResponse(uuid, invocation) as R
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
    val uuid = this.uuid
    MockingBird.invocationRecorder().access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(uuid, recorder, invocation)
        recorder.getResponse(
            uuid = uuid,
            invocation = invocation,
            relaxed = relaxed
        )
    }
}

/**
 * Convenient function to spy methods
 * @param methodName name of the method that you want to spy
 * @param arguments map between names and method arguments
 * @param delegate lambda that will be invoked if no mocked response is present for the function call (this lambda should delegate the call to the real implementation)
 * @return returns the mocked result for the method call described by arguments above if the method
 * has been mocked, the result of the real invocation otherwise
 */
public fun <T : Spy, R> T.spy(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    delegate: () -> R
): R {
    val uuid = this.uuid
    return MockingBird.invocationRecorder().access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(uuid, recorder, invocation)// TODO change name
        @Suppress("UNCHECKED_CAST")
        val mockResponse = recorder.getResponse(
            uuid = uuid,
            invocation = invocation,
            relaxed = true
        ) as R
        mockResponse ?: delegate()
    }
}

/**
 * Convenient function to spy methods
 * @param methodName name of the method that you want to spy
 * @param arguments map between names and method arguments
 * @param delegate lambda that will be invoked if no mocked response is present for the function call (this lambda should delegate the call to the real implementation)
 * @return returns the mocked result for the method call described by arguments above if the method
 * has been mocked, the result of the real invocation otherwise
 */
public suspend fun <T : Spy, R> T.suspendSpy(
    methodName: String,
    arguments: Map<String, Any?> = emptyMap(),
    delegate: suspend () -> R
): R {
    val uuid = this.uuid
    return MockingBird.invocationRecorder().access { recorder ->
        val invocation = Invocation(methodName = methodName, arguments = arguments)
        recordInvocation(uuid, recorder, invocation)
        @Suppress("UNCHECKED_CAST")
        recorder.getResponse(
            uuid = uuid,
            invocation = invocation,
            relaxed = true
        ) as R
    } ?: delegate()
}


public fun <T : Mock> T.uuid(): Lazy<String> {
    return lazy { "${this.hashCode()}-${uuidGenerator.getAndAdd(1)}" }
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

private fun recordInvocation(uuid: String, recorder: InvocationRecorder, invocation: Invocation) {
    recorder.storeInvocation(
        uuid = uuid,
        invocation = invocation
    )
}