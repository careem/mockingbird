package com.careem.mockingbird.test

import com.careem.mockingbird.test.Mocks.MyDependencyMock
import com.careem.mockingbird.test.Mocks.TEST_INT
import com.careem.mockingbird.test.Mocks.TEST_STRING
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FunctionsTest {

    @Test
    fun `test everyAnswer when mock called from worker`() {
        val testMock = Mocks.MyDependencyMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = Mocks.MyDependencyMock.Method.method1,
            arguments = mapOf(Mocks.MyDependencyMock.Arg.str to Mocks.TEST_STRING)
        ) {
            iWillBeSet.value = Mocks.TEST_STRING
        }
        assertNull(iWillBeSet.value)

        runOnWorker {
            testMock.method1(Mocks.TEST_STRING)
        }

        testMock.verify(
            methodName = Mocks.MyDependencyMock.Method.method1,
            arguments = mapOf(Mocks.MyDependencyMock.Arg.str to Mocks.TEST_STRING)
        )
        assertEquals(Mocks.TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun `test everyAnswer when no args and answer returns a value`() {
        val testMock = Mocks.MyDependencyMock()
        testMock.everyAnswers(
            methodName = Mocks.MyDependencyMock.Method.method4
        ) {
            return@everyAnswers 5
        }

        val value = testMock.method4()
        testMock.verify(
            methodName = Mocks.MyDependencyMock.Method.method4
        )
        assertEquals(5, value)
    }

    @Test
    fun `test everyAnswer`() {
        val testMock = Mocks.MyDependencyMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = Mocks.MyDependencyMock.Method.method1,
            arguments = mapOf(Mocks.MyDependencyMock.Arg.str to Mocks.TEST_STRING)
        ) {
            iWillBeSet.value = Mocks.TEST_STRING
        }
        assertNull(iWillBeSet.value)

        testMock.method1(Mocks.TEST_STRING)
        testMock.verify(
            methodName = Mocks.MyDependencyMock.Method.method1,
            arguments = mapOf(Mocks.MyDependencyMock.Arg.str to Mocks.TEST_STRING)
        )
        assertEquals(Mocks.TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun `test capture slot`() {
        val testMock = MyDependencyMock()
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
    fun `test any() matcher works properly for every()`() {
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
        assertEquals(1, testMock.method3(TEST_INT, TEST_INT))
    }

    @Test
    fun `test any() matcher works properly for verify()`() {
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
}