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

    @Test
    fun testMockNotFrozenOnMock() {
        val testMock = MyDependencyMock()
        testMock.every(
            methodName = MyDependencyMock.Method.method4
        ) { 1 }
        testMock.method4()
        assertFalse{ testMock.isFrozen }
    }

    @Test
    fun testMockNotFrozenOnMockUnit() {
        val testMock = MyDependencyMock()
        testMock.method1("str")
        assertFalse{ testMock.isFrozen }
    }
}