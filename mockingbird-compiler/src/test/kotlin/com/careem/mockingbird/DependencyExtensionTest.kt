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

package com.careem.mockingbird

import io.mockk.every
import io.mockk.mockk
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.invocation.Gradle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DependencyExtensionTest {

    private val gradle = mockk<Gradle>()

    @Before
    fun setup() {
        every { gradle.gradleUserHomeDir.absolutePath } returns TEST_ABSOLUTE_PATH
    }

    @Test
    fun testGetArtifactPath() {
        val dependency = DefaultExternalModuleDependency(TEST_GROUP_NAME, TEST_DEPENDENCY_NAME, TEST_DEPENDENCY_VERSION)
        assertEquals(
            "${TEST_ABSOLUTE_PATH}/caches/modules-2/files-2.1/${TEST_GROUP_NAME}/${TEST_DEPENDENCY_NAME}-jvm/$TEST_DEPENDENCY_VERSION",
            dependency.artifactPath(gradle)
        )
    }

    companion object {
        private const val TEST_ABSOLUTE_PATH = "test/path"
        private const val TEST_GROUP_NAME = "testGroup"
        private const val TEST_DEPENDENCY_NAME = "testDependency"
        private const val TEST_DEPENDENCY_VERSION = "1.0"
    }
}