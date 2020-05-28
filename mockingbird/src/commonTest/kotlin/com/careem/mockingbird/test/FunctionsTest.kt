package com.careem.mockingbird.test

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FunctionsTest {

    @Test
    fun `test every when mock called from worker`() {

        helloWorld()
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { 1 }

        val value = runOnWorker {
            testMock.method3(TEST_INT, TEST_INT)
        }

        assertEquals(1, value)
    }

    @Test
    fun `test every when no args`() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method4
        ) { 1 }

        val value = testMock.method4()

        assertEquals(1, value)
    }

    @Test
    fun `test every`() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { 1 }

        val value = testMock.method3(TEST_INT, TEST_INT)

        assertEquals(1, value)
    }

    @Test
    fun `test everyAnswer when mock called from worker`() {
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
    fun `test everyAnswer when no args and answer returns a value`() {
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
    fun `test everyAnswer`() {
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

    companion object {
        private const val TEST_STRING = "test_string"
        private const val TEST_INT = 3

        interface MyDependency {
            fun method1(str: String)
            fun method2(str: String, value: Int)
            fun method3(value1: Int, value2: Int): Int
            fun method4(): Int
        }

        class MyDependencyMock : MyDependency, Mock {
            object Method {
                const val method1 = "method1"
                const val method2 = "method2"
                const val method3 = "method3"
                const val method4 = "method4"
            }

            object Arg {
                const val str = "str"
                const val value = "value"
                const val value1 = "value1"
                const val value2 = "value2"
            }

            override fun method1(str: String) = mockUnit(
                methodName = Method.method1,
                arguments = mapOf(
                    Arg.str to str
                )
            )

            override fun method2(str: String, value: Int) = mockUnit(
                methodName = Method.method2,
                arguments = mapOf(
                    Arg.str to str,
                    Arg.value to value
                )
            )

            override fun method3(value1: Int, value2: Int): Int = mock(
                methodName = Method.method3,
                arguments = mapOf(
                    Arg.value1 to value1,
                    Arg.value2 to value2
                )
            )

            override fun method4(): Int = mock(
                methodName = Method.method4
            )
        }
    }
}