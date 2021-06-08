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

import com.careem.mockingbird.test.Mocks.MyDependencyImpl
import com.careem.mockingbird.test.Mocks.MyDependencySpy
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpyTest {

    /**
     * Uncomment this test when issue:
     * https://github.com/careem/mockingbird/issues/23
     * will be fixed
     */
    @Test
    @Ignore
    fun testSpyNotFrozenWhenSpyFunctionIsCalled() {
        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)
        spy.method1("str")
        assertFalse{ spy.isFrozen }
    }


    /**
     * Uncomment this test when issue:
     * https://github.com/careem/mockingbird/issues/23
     * will be fixed
     */
    @Test
    @Ignore
    fun testSpyNotFrozenWhenSpyFunctionIsCalledAndBehaviorMocked() {
        val expect = 9

        val realImpl = MyDependencyImpl()
        val spy = MyDependencySpy(realImpl)
        spy.every(
            methodName = MyDependencySpy.Method.method4
        ) { expect }

        spy.method4()
        assertFalse{ spy.isFrozen }
    }

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

