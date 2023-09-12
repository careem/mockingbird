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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import kotlin.test.assertEquals

class ProjectExtensionTest {

    private val testProject = generateTestProject()

    @Test
    fun testGetFullQualifier() {
        assertEquals("${TEST_GROUP}:${TEST_PROJECT_NAME}", testProject.fullQualifier())
        assertEquals("${TEST_PROJECT_NAME}:${TEST_SUBPROJECT_NAME}", testProject.subprojects.first().fullQualifier())
    }

    @Test
    fun testGetClassPath() {
        assertEquals(
            "${testProject.layout.buildDirectory.asFile.get().absolutePath}/classes/kotlin/jvm/main",
            testProject.classPath()
        )
    }

    @Test
    fun testGetThirdPartiesClassPath() {
        assertEquals(
            "${testProject.layout.buildDirectory.asFile.get().absolutePath}/dependencies",
            testProject.thirdPartiesClassPath()
        )
    }

    private fun generateTestProject(): Project {
        val projectBuilder = ProjectBuilder.builder()
        val testProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        testProject.group = TEST_GROUP
        projectBuilder.withName(TEST_SUBPROJECT_NAME).withParent(testProject).build()
        return testProject
    }

    companion object {
        private const val TEST_GROUP = "TEST_GROUP"
        private const val TEST_PROJECT_NAME = "TEST_PROJECT_NAME"
        private const val TEST_SUBPROJECT_NAME = "TEST_SUBPROJECT_NAME"
    }
}