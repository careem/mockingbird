package com.careem.mockingbird.test

import com.careem.mockingbird.test.Mocks.TEST_INT
import com.careem.mockingbird.test.Mocks.MyDependencyMock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EveryTest {

    @Test
    fun testEveryWhenMockCalledFromWorker() {
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
    fun testMockNotFrozenOnEvery() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method3,
            arguments = mapOf(
                MyDependencyMock.Arg.value1 to TEST_INT,
                MyDependencyMock.Arg.value2 to TEST_INT
            )
        ) { 1 }

        assertFalse { testMock.isFrozen }
    }

    @Test
    fun testEveryWhenNoArgs() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method4
        ) { 1 }

        val value = testMock.method4()

        assertEquals(1, value)
    }

    @Test
    fun testEvery() {
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
}