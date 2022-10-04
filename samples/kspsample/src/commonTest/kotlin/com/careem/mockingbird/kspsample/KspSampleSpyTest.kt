/*
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.annotations.Spy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import com.careem.mockingbird.test.every
import com.careem.mockingbird.test.any

class KspSampleSpyTest {

    @Spy
    val suspendInterfaceSpy: SuspendInterface = SuspendInterfaceSpy(SuspendInterfaceImpl())

    @Spy
    lateinit var pippoSampleSpy: PippoSample

    @Spy
    lateinit var outerInterfaceSpy: OuterInterface

    @Spy
    lateinit var multipleGetterProperties: MultipleGetterProperties

    @Spy
    lateinit var mockWithExternalDependencies: MockWithExternalDependencies

    @Spy
    lateinit var mock1: Mock1

    @Spy
    lateinit var lambdaSample: LambdaSample

    @Spy
    lateinit var javaTypes: JavaTypes

    @Spy
    internal lateinit var internalSampleInterface: InternalSampleInterface

    @Spy
    lateinit var interfaceWithGenerics: InterfaceWithGenerics

    @Spy
    lateinit var innerInterface: InnerInterface

    @Spy
    lateinit var innerInnerInterface: InnerInnerInterface

    @Spy
    lateinit var uiDelegate: UiDelegate<UiState>

    @Spy
    lateinit var uiDelegate2Args: UiDelegate2Args<UiState, Value>

    @Test
    fun testSuspendSpy() = runBlocking {
        // Verify delegate suspend is working
        val suspendResult = suspendInterfaceSpy.suspendFunction("a")
        assertEquals("defered", suspendResult)

        // Verifying mocking the spy works as expected
        suspendInterfaceSpy.every(
            methodName = SuspendInterfaceSpy.Method.suspendFunction,
            arguments = mapOf(SuspendInterfaceSpy.Arg.a to any())
        ){
            "mocked"
        }
        val suspendResultMocked = suspendInterfaceSpy.suspendFunction("a")
        assertEquals("mocked", suspendResultMocked)


    }

    class SuspendInterfaceImpl : SuspendInterface {
        override suspend fun suspendFunction(a: String): String {
            delay(1)
            return "defered"
        }
    }

}

