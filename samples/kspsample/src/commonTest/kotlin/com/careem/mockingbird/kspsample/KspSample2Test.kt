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
import kotlin.test.Test
import kotlin.test.assertNotNull

class KspSample2Test {
    @Mock
    val pippoMock: PippoSample = PippoSampleMock() // This class was requested to be mocked also on the other test file

    @Mock
    val usedOnceMock: UsedOnce = UsedOnceMock() // Used to validate a class to mock that is used only on this test

    @Test
    fun testGeneratedTargetProjectMock() {
        assertNotNull(pippoMock)
    }

}

