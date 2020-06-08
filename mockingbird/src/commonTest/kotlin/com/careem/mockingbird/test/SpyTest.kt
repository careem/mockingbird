package com.careem.mockingbird.test

import com.careem.mockingbird.test.Mocks.MyDependencyImpl
import com.careem.mockingbird.test.Mocks.MyDependencySpy
import kotlin.test.Test
import kotlin.test.assertEquals

class SpyTest {

    @Test
    fun testSpyCallsRealFunctionWhenNoMockProvided() {
        val value1 = 3
        val value2 = 4
        val expect = value1 + value2

        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)

        val res = spy.method3(value1, value2)

        assertEquals(expect, res)
    }

    @Test
    fun testSpyCallsMockedFunctionWhenMockProvidedAndDefaultArgs() {
        val expect = 9

        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)
        spy.every(
            methodName = MyDependencySpy.Method.method4
        ) { expect }

        val res = spy.method4()

        assertEquals(expect, res)
    }

    @Test
    fun testSpyCallsMockedFunctionWhenMockProvidedForSpecificArgs() {
        val value1 = 3
        val value2 = 4
        val expect = value1 - value2

        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)
        spy.every(
            methodName = MyDependencySpy.Method.method3,
            arguments = mapOf(
                MyDependencySpy.Arg.value1 to value1,
                MyDependencySpy.Arg.value2 to value2
            )
        ) { value1 - value2 }

        val res = spy.method3(value1, value2)

        assertEquals(expect, res)
    }

    @Test
    fun testSpyCallsRealFunctionWhenNoMockProvidedForSpecificArgs() {
        val value1 = 3
        val value2 = 4
        val expect = value1 + value2

        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)
        spy.every(
            methodName = MyDependencySpy.Method.method3,
            arguments = mapOf(
                MyDependencySpy.Arg.value1 to 7,
                MyDependencySpy.Arg.value2 to value2
            )
        ) { value1 - value2 }

        val res = spy.method3(value1, value2)

        assertEquals(expect, res)
    }

    @Test
    fun testVerifyRealFunctionCalledOnSpiedObjectWhenNoMockProvided() {
        val value1 = 3
        val value2 = 4
        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)

        spy.method3(value1, value2)

        spy.verify(
            methodName = MyDependencySpy.Method.method3,
            arguments = mapOf(
                MyDependencySpy.Arg.value1 to value1,
                MyDependencySpy.Arg.value2 to value2
            )
        )
    }
}

