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

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(KotlinPoetMetadataPreview::class)
class ClassLoaderWrapperTest {

    private val projectExplorer = mockk<ProjectExplorer>()
    private val testProject = generateTestProject()
    private lateinit var classLoaderWrapper: ClassLoaderWrapper

    @Before
    fun setup() {
        every { projectExplorer.explore(any()) } returns generateDependencySet()
        classLoaderWrapper = ClassLoaderWrapper(projectExplorer, testProject)
    }

    @Test
    fun testInitializeClassLoaderWrapperWithDefaultExternalModuleDependency() {
        val kotlinClass = classLoaderWrapper.loadClass(DefaultExternalModuleDependency::class.qualifiedName!!)
        assertEquals(DefaultExternalModuleDependency::class, kotlinClass)
    }

    private fun generateTestProject(): Project {
        val projectBuilder = ProjectBuilder.builder()
        val testProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_1").withParent(testProject).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_2").withParent(testProject).build()
        testProject.group = TEST_GROUP_NAME
        return testProject
    }

    private fun generateDependencySet(): Set<Dependency> {
        val dependency = DefaultExternalModuleDependency(TEST_GROUP_NAME, TEST_DEPENDENCY_NAME, TEST_DEPENDENCY_VERSION)
        return setOf(dependency)
    }

    companion object {
        private const val TEST_GROUP_NAME = "testGroup"
        private const val TEST_DEPENDENCY_NAME = "testDependency"
        private const val TEST_DEPENDENCY_VERSION = "1.0"
        private const val TEST_PROJECT_NAME = "testProject"
        private const val TEST_SUBPROJECT_NAME = "testSubproject"
    }
}