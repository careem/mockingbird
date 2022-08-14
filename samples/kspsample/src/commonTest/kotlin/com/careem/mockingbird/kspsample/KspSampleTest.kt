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

import com.careem.mockingbird.test.annotations.Mock

class KspSampleTest {
    @Mock
    lateinit var pippoMock: PippoSample

    @Mock
    lateinit var outerInterface: OuterInterface

    @Mock
    lateinit var multipleGetterProperties: MultipleGetterProperties

    @Mock
    lateinit var mockWithExternalDependencies: MockWithExternalDependencies

    @Mock
    lateinit var mock1: Mock1

    @Mock
    lateinit var lambdaSample: LambdaSample

    @Mock
    lateinit var javaTypes: JavaTypes

    @Mock
    lateinit var internalSampleInterface: InternalSampleInterface

    @Mock
    lateinit var interfaceWithGenerics: InterfaceWithGenerics

    @Mock
    lateinit var innerInterface: InnerInterface

    @Mock
    lateinit var innerInnerInterface: InnerInnerInterface

}

