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

package com.careem.mockingbird.sample

import com.careem.mockingbird.ExternalContractMock
import com.careem.mockingbird.MockWithExternalDependenciesMock
import com.careem.mockingbird.PippoSampleMock
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestClass {

    @Test
    fun testGeneratedTargetProjectMock() {
        val pippoMock: PippoSample = PippoSampleMock()
        assertNotNull(pippoMock)
    }


    @Test
    fun testGeneratedTargetProjectDependentMock() {
        val externalMock = MockWithExternalDependenciesMock()
        assertNotNull(externalMock)
    }

    @Test
    fun testGeneratedTargetProjectExternalMock() {
        val externalContractMock = ExternalContractMock()
        assertNotNull(externalContractMock)
    }

}

