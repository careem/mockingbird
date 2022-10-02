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
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class GeneratedFilesTest {

    @Mock
    val pippoMock: PippoSample = PippoSampleMock()

    @Mock
    val outerInterface: OuterInterface = OuterInterfaceMock()

    @Mock
    val multipleGetterProperties: MultipleGetterProperties = MultipleGetterPropertiesMock()

    @Mock
    val mockWithExternalDependencies: MockWithExternalDependencies = MockWithExternalDependenciesMock()

    @Mock
    val mock1: Mock1 = Mock1Mock()

    @Mock
    val lambdaSample: LambdaSample = LambdaSampleMock()

    @Mock
    val javaTypes: JavaTypes = JavaTypesMock()

    @Mock
    private val internalSampleInterface: InternalSampleInterface = InternalSampleInterfaceMock()

    @Mock
    val interfaceWithGenerics: InterfaceWithGenerics = InterfaceWithGenericsMock()

    @Mock
    val innerInterface: InnerInterface = InnerInterfaceMock()

    @Mock
    val innerInnerInterface: InnerInnerInterface = InnerInnerInterfaceMock()

    @Mock
    val uiDelegate: UiDelegate<UiState> = UiDelegate_UiStateMock()

    @Mock
    val uiDelegate2Args: UiDelegate2Args<UiState, Value> = UiDelegate2Args_UiState_ValueMock()

    @Test
    fun testFileGeneration() {
        val expectFolder = expectedCodeGenFolder()
        val actualFolder = actualCodeGenFolder()

        assertEquals(actualFolder.listFiles()!!.size, expectFolder.listFiles()!!.size)

        expectFolder.listFiles()!!.forEach { expect ->
            val actual = lookUpActual(actualFolder, expect)
            assertEquals(expect.readText(), actual.readText())
        }
    }

    private fun expectedCodeGenFolder(): File = File("src/jvmTest/resources/mocks/com/careem/mockingbird/kspsample")
    private fun actualCodeGenFolder(): File = File("build/generated/ksp/jvm/jvmTest/kotlin/com/careem/mockingbird/kspsample")
    private fun lookUpActual(actualFolder: File, expectFile: File): File {
        return File("${actualFolder.path}/${expectFile.name}")
    }
}