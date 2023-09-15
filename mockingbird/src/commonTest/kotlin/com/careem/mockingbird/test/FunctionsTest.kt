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
@file:Suppress("DEPRECATION")

package com.careem.mockingbird.test

import com.careem.mockingbird.test.Mocks.MyDependencyMock
import com.careem.mockingbird.test.Mocks.TEST_INT
import com.careem.mockingbird.test.Mocks.TEST_STRING
import com.careem.mockingbird.util.getSystemTimeInMillis
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunctionsTest {

    @Test
    fun testLocalModeDoNotFreezeClass() = runWithTestMode(TestMode.LOCAL_THREAD){
        val myDependencyMock = MyDependencyMock()
        myDependencyMock.everyAnswers(
            methodName = MyDependencyMock.Method.method6,
            arguments = mapOf(
                MyDependencyMock.Arg.callback to any()
            )
        ){ it.getArgument<() -> Unit>(MyDependencyMock.Arg.callback).invoke() }

        val instance = LocalThreadAccessibleClass(myDependencyMock)
        instance.execute()

        myDependencyMock.verify(
            methodName = MyDependencyMock.Method.method6,
            arguments = mapOf(Mocks.MyDependencySpy.Arg.callback to any())
        )
        assertEquals(1, instance.counter)
    }

    @Test
    fun testEveryAnswerWhenMockCalledFromWorker() {
        val testMock = MyDependencyMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {
            iWillBeSet.value = TEST_STRING
        }
        assertNull(iWillBeSet.value)

        runOnWorker {
            testMock.method1(TEST_STRING)
        }

        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        )
        assertEquals(TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun testEveryAnswerWhenNoArgsAndAnswerReturnsAValue() {
        val testMock = MyDependencyMock()
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method4
        ) {
            return@everyAnswers 5
        }

        val value = testMock.method4()
        testMock.verify(
            methodName = MyDependencyMock.Method.method4
        )
        assertEquals(5, value)
    }

    @Test
    fun testEveryAnswer() {
        val testMock = MyDependencyMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {
            iWillBeSet.value = TEST_STRING
        }
        assertNull(iWillBeSet.value)

        testMock.method1(TEST_STRING)
        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        )
        assertEquals(TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun testCaptureSlotWithCorrectArgument() {
        val testMock = MyDependencyMock()
        val stringSlot = slot<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(stringSlot))
        )

        assertEquals(TEST_STRING, stringSlot.captured)
    }

    @Test
    fun testCaptureSlotWithCorrectArgumentDeprecated() {
        val testMock = MyDependencyMock()

        @Suppress("DEPRECATION")
        val stringSlot = Slot<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(stringSlot))
        )

        assertEquals(TEST_STRING, stringSlot.captured)
    }


    @Test
    fun testCaptureLocalSlotWithCorrectArgumentDeprecated() = runWithTestMode(TestMode.LOCAL_THREAD) {
        val testMock = MyDependencyMock()

        @Suppress("DEPRECATION")
        val stringSlot = Slot<String>()

        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(stringSlot))
        )
        assertEquals(TEST_STRING, stringSlot.captured)
    }


    @Test
    fun testCaptureLocalSlotWithCorrectArgument() = runWithTestMode(TestMode.LOCAL_THREAD) {
        val testMock = MyDependencyMock()

        val stringSlot = slot<String>()

        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.verify(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(stringSlot))
        )
        assertEquals(TEST_STRING, stringSlot.captured)
    }

    @Test
    fun testCaptureSlotWithWrongArgumentDeprecated() {
        val testMock = MyDependencyMock()

        @Suppress("DEPRECATION")
        val stringSlot = Slot<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        try {
            testMock.method1(TEST_STRING)
            testMock.verify(
                methodName = MyDependencyMock.Method.method1,
                arguments = mapOf(MyDependencyMock.Arg.value to capture(stringSlot))
            )
        } catch (error: AssertionError) {
            assertNotNull(error)
        }
    }

    @Test
    fun testCaptureSlotWithWrongArgument() {
        val testMock = MyDependencyMock()

        val stringSlot = slot<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        try {
            testMock.method1(TEST_STRING)
            testMock.verify(
                methodName = MyDependencyMock.Method.method1,
                arguments = mapOf(MyDependencyMock.Arg.value to capture(stringSlot))
            )
        } catch (error: AssertionError) {
            assertNotNull(error)
        }
    }

    @Test
    fun testCapturedListSucceed() {
        val testMock = MyDependencyMock()

        val capturedList = capturedList<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.method1(TEST_STRING)
        testMock.method1(TEST_STRING)
        testMock.verify(
            exactly = 3,
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(capturedList))
        )
        assertEquals(3, capturedList.captured.size)
        assertEquals(TEST_STRING, capturedList.captured[0])
        assertEquals(TEST_STRING, capturedList.captured[1])
        assertEquals(TEST_STRING, capturedList.captured[2])
    }

    @Test
    fun testCapturedListSucceedOnLocalThread() = runWithTestMode(TestMode.LOCAL_THREAD) {
        val testMock = MyDependencyMock()
        val capturedList = capturedList<String>()
        testMock.every(
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
        ) {}

        testMock.method1(TEST_STRING)
        testMock.method1(TEST_STRING)
        testMock.method1(TEST_STRING)
        testMock.verify(
            exactly = 3,
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to capture(capturedList))
        )
        assertEquals(3, capturedList.captured.size)
        assertEquals(TEST_STRING, capturedList.captured[0])
        assertEquals(TEST_STRING, capturedList.captured[1])
        assertEquals(TEST_STRING, capturedList.captured[2])
    }

    @Test
    fun testAnyMatcherWorksProperlyForEvery() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { 1 }
        assertEquals(1, testMock.method3(TEST_INT, TEST_INT))

        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to any(),
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { 2 }
        assertEquals(2, testMock.method3(100, TEST_INT))

        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to any()
            )
        ) { 3 }
        assertEquals(3, testMock.method3(TEST_INT, 200))

        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to any(),
                MyDependencyMock.Arg.value2 to any()
            )
        ) { 4 }
        assertEquals(4, testMock.method3(300, 200))
        assertEquals(4, testMock.method3(TEST_INT, TEST_INT))
    }

    @Test
    fun testAnyMatcherWorksProperlyForVerify() {
        val testMock = MyDependencyMock()
        testMock.method1(TEST_STRING)
        testMock.verify(
            exactly = 1,
            methodName = MyDependencyMock.Method.method1,
            arguments = mapOf(MyDependencyMock.Arg.str to any())
        )
        testMock.verify(
            exactly = 0,
            methodName = MyDependencyMock.Method.method2,
            arguments = mapOf(
                MyDependencyMock.Arg.str to any(),
                MyDependencyMock.Arg.value to any()
            )
        )

        testMock.method2(TEST_STRING, TEST_INT)
        testMock.verify(
            exactly = 1,
            methodName = MyDependencyMock.Method.method2,
            arguments = mapOf(
                MyDependencyMock.Arg.str to any(),
                MyDependencyMock.Arg.value to TEST_INT
            )
        )
        testMock.verify(
            exactly = 1,
            methodName = MyDependencyMock.Method.method2,
            arguments = mapOf(
                MyDependencyMock.Arg.str to TEST_STRING,
                MyDependencyMock.Arg.value to any()
            )
        )
        testMock.verify(
            exactly = 1,
            methodName = MyDependencyMock.Method.method2,
            arguments = mapOf(
                MyDependencyMock.Arg.str to any(),
                MyDependencyMock.Arg.value to any()
            )
        )
        testMock.verify(
            exactly = 1,
            methodName = MyDependencyMock.Method.method2,
            arguments = mapOf(
                MyDependencyMock.Arg.str to TEST_STRING,
                MyDependencyMock.Arg.value to TEST_INT
            )
        )
    }

    @Test
    fun testThrowAssertErrorWhenArgumentSizeIsWrong() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { TEST_INT }

        try {
            testMock.method3(TEST_INT, TEST_INT)
            testMock.verify(
                methodName = MyDependencyMock.Method.method3,
                arguments = mapOf(
                    MyDependencyMock.Arg.value1 to TEST_INT
                )
            )
        } catch (error: AssertionError) {
            assertNotNull(error)
        }
    }

    @Test
    fun testThrowAssertErrorWhenArgumentKeyIsWrong() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { TEST_INT }

        try {
            testMock.method3(TEST_INT, TEST_INT)
            testMock.verify(
                methodName = MyDependencyMock.Method.method3,
                arguments = mapOf(
                    MyDependencyMock.Arg.value1 to TEST_INT,
                    "wrong_key" to TEST_INT
                )
            )
        } catch (error: AssertionError) {
            assertNotNull(error)
        }
    }

    @Test
    fun testThrowAssertErrorWhenArgumentValueIsWrong() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { TEST_INT }

        try {
            testMock.method3(TEST_INT, TEST_INT)
            testMock.verify(
                methodName = MyDependencyMock.Method.method3,
                arguments = mapOf(
                    MyDependencyMock.Arg.value1 to TEST_INT,
                    MyDependencyMock.Arg.value2 to "wrong_value"
                )
            )
        } catch (error: AssertionError) {
            assertNotNull(error)
        }
    }

    @Test
    fun testNoNeedToCallEveryOnUnitFunctionWithoutArguments() {
        val testMock = MyDependencyMock()
        testMock.method5()
        testMock.verify(
            methodName = MyDependencyMock.Method.method5
        )
    }

    @Test
    fun testVerifyFailedAfterTimeout() {
        val testMock = MyDependencyMock()
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method4
        ) {
            return@everyAnswers 5
        }

        testMock.method4()
        val verifyStartTime = getSystemTimeInMillis()
        try {
            testMock.verify(
                timeoutMillis = VERIFY_TIMEOUT,
                methodName = MyDependencyMock.Method.method5
            )
        } catch (e: AssertionError) {
        } finally {
            val verifyEndTime = getSystemTimeInMillis()

            println(verifyStartTime)
            println(verifyEndTime)

            assertTrue { verifyEndTime - verifyStartTime > VERIFY_TIMEOUT }
        }
    }

    @Test
    fun testVerifySuccessBeforeTimeout() {
        val testMock = MyDependencyMock()
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method4
        ) {
            return@everyAnswers 5
        }

        testMock.method4()

        val verifyStartTime = getSystemTimeInMillis()
        testMock.verify(
            timeoutMillis = VERIFY_TIMEOUT,
            methodName = MyDependencyMock.Method.method4
        )
        val verifyEndTime = getSystemTimeInMillis()

        println(verifyStartTime)
        println(verifyEndTime)

        assertTrue { verifyEndTime - verifyStartTime < VERIFY_TIMEOUT }
    }


    @Test
    fun testLastRecordedInvocationRetrivedIfMultipleMatches() {
        val a = 2
        val b = 3
        val testMock = MyDependencyMock()
        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to any(),
                MyDependencyMock.Arg.value2 to any()
            )
        ) {
            return@everyAnswers a
        }

        assertEquals(a, testMock.method3(0, 1))

        testMock.everyAnswers(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to any(),
                MyDependencyMock.Arg.value2 to any()
            )
        ) {
            return@everyAnswers b
        }

        assertEquals(b, testMock.method3(0, 1))
    }

    companion object {
        const val VERIFY_TIMEOUT = 500L
    }
}