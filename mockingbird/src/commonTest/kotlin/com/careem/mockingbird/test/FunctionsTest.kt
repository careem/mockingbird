package com.careem.mockingbird.test

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FunctionsTest {

    @Test
    fun `test every when mock called from worker`() {
        val testMock = TestMock()
        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to TEST_INT, TestMock.Arg.value2 to TEST_INT)
        ) { 1 }

        val value = threadedTest {
            testMock.testMethod3(TEST_INT, TEST_INT)
        }

        assertEquals(1, value)
    }

    @Test
    fun `test every`() {
        val testMock = TestMock()
        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to TEST_INT, TestMock.Arg.value2 to TEST_INT)
        ) { 1 }

        val value = testMock.testMethod3(TEST_INT, TEST_INT)

        assertEquals(1, value)
    }

    @Test
    fun `test everyAnswer when mock called from worker`() {
        val testMock = TestMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING)
        ) {
            iWillBeSet.value = TEST_STRING
        }
        assertNull(iWillBeSet.value)

        threadedTest {
            testMock.testMethod1(TEST_STRING)
        }

        testMock.verify(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING)
        )
        assertEquals(TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun `test everyAnswer`() {
        val testMock = TestMock()
        val iWillBeSet: AtomicRef<String?> = atomic(null)
        testMock.everyAnswers(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING)
        ) {
            iWillBeSet.value = TEST_STRING
        }
        assertNull(iWillBeSet.value)

        testMock.testMethod1(TEST_STRING)
        testMock.verify(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING)
        )
        assertEquals(TEST_STRING, iWillBeSet.value)
    }

    @Test
    fun `test capture slot`() {
        val testMock = TestMock()
        val stringSlot = Slot<String>()
        testMock.every(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING)
        ) {}

        testMock.testMethod1(TEST_STRING)
        testMock.verify(
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to capture(stringSlot))
        )
        assertEquals(TEST_STRING, stringSlot.captured)
    }

    @Test
    fun `test any() matcher works properly for every()`() {
        val testMock = TestMock()
        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to TEST_INT, TestMock.Arg.value2 to TEST_INT)
        ) { 1 }
        assertEquals(1, testMock.testMethod3(TEST_INT, TEST_INT))

        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to any(), TestMock.Arg.value2 to TEST_INT)
        ) { 2 }
        assertEquals(2, testMock.testMethod3(100, TEST_INT))

        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to TEST_INT, TestMock.Arg.value2 to any())
        ) { 3 }
        assertEquals(3, testMock.testMethod3(TEST_INT, 200))

        testMock.every(
            methodName = TestMock.Method.testMethod3,
            arguments = mapOf(TestMock.Arg.value1 to any(), TestMock.Arg.value2 to any())
        ) { 4 }
        assertEquals(4, testMock.testMethod3(300, 200))
        assertEquals(1, testMock.testMethod3(TEST_INT, TEST_INT))
    }

    @Test
    fun `test any() matcher works properly for verify()`() {
        val testMock = TestMock()
        testMock.testMethod1(TEST_STRING)
        testMock.verify(
            exactly = 1,
            methodName = TestMock.Method.testMethod1,
            arguments = mapOf(TestMock.Arg.str to any())
        )
        testMock.verify(
            exactly = 0,
            methodName = TestMock.Method.testMethod2,
            arguments = mapOf(TestMock.Arg.str to any(), TestMock.Arg.value to any())
        )

        testMock.testMethod2(TEST_STRING, TEST_INT)
        testMock.verify(
            exactly = 1,
            methodName = TestMock.Method.testMethod2,
            arguments = mapOf(TestMock.Arg.str to any(), TestMock.Arg.value to TEST_INT)
        )
        testMock.verify(
            exactly = 1,
            methodName = TestMock.Method.testMethod2,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING, TestMock.Arg.value to any())
        )
        testMock.verify(
            exactly = 1,
            methodName = TestMock.Method.testMethod2,
            arguments = mapOf(TestMock.Arg.str to any(), TestMock.Arg.value to any())
        )
        testMock.verify(
            exactly = 1,
            methodName = TestMock.Method.testMethod2,
            arguments = mapOf(TestMock.Arg.str to TEST_STRING, TestMock.Arg.value to TEST_INT)
        )
    }

    companion object {
        private const val TEST_STRING = "test_string"
        private const val TEST_STRING_2 = "test_string_2"
        private const val TEST_INT = 3

        interface TestInterface {
            fun testMethod1(str: String)
            fun testMethod2(str: String, value: Int)
            fun testMethod3(value1: Int, value2: Int): Int
        }

        class TestMock : TestInterface, Mock {
            object Method {
                const val testMethod1 = "testMethod1"
                const val testMethod2 = "testMethod2"
                const val testMethod3 = "testMethod3"
            }

            object Arg {
                const val str = "string"
                const val value = "value"
                const val value1 = "value1"
                const val value2 = "value2"
            }

            override fun testMethod1(str: String) = mockUnit(
                methodName = Method.testMethod1,
                arguments = mapOf(
                    Arg.str to str
                )
            )

            override fun testMethod2(str: String, value: Int) = mockUnit(
                methodName = Method.testMethod2,
                arguments = mapOf(
                    Arg.str to str,
                    Arg.value to value
                )
            )

            override fun testMethod3(value1: Int, value2: Int): Int = mock(
                methodName = Method.testMethod3,
                arguments = mapOf(
                    Arg.value1 to value1,
                    Arg.value2 to value2
                )
            )
        }
    }
}