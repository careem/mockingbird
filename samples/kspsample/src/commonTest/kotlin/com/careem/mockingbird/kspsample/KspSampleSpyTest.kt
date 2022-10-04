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
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KspSampleSpyTest {

    val pippoSample = object : PippoSample{
        override val currentSession: Int
            get() = TODO("Not yet implemented")
        override var currentMutableSession: Int
            get() = TODO("Not yet implemented")
            set(value) {}

        override fun showRandom(): Boolean {
            TODO("Not yet implemented")
        }

        override fun sayHi() {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String) {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String, someOtherParam: Boolean) {
            TODO("Not yet implemented")
        }

        override fun sayHiWithCommonParam(param: String, intParam: Int) {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String, map: Map<String, String>) {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String, entry: Map.Entry<String, String>) {
            TODO("Not yet implemented")
        }

        override suspend fun thisIsSuspend(param: String, intParam: Int) {
            TODO("Not yet implemented")
        }
    }

//    @Spy
//    lateinit var pippoMock: PippoSample

    val pippoSpy: PippoSample = PippoSample2Spy(pippoSample)

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
    fun test(){
        assertNull(null)
    }

}

