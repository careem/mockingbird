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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectExplorerTest {

    private val sourceSetResolver = mockk<SourceSetResolver>()
    private val testProject = generateTestProject()
    private lateinit var projectExplorer: ProjectExplorer

    @Before
    fun setup() {
        val testKotlinSourceSet = DefaultKotlinSourceSet(testProject, TEST_SOURCE_SET)
        every { sourceSetResolver.getSourceSetFromKmpExtension(any(), any()) } returns testKotlinSourceSet
        projectExplorer = ProjectExplorer(sourceSetResolver)
    }

    @Test
    fun testVisitRootProject() {
        assertTrue { projectExplorer.moduleMap.isEmpty() }
        projectExplorer.visitRootProject(testProject)
        assertEquals(3, projectExplorer.moduleMap.size)
    }

    @Test
    fun testExploreWithKotlinMultiplatformExtension() {
        assertTrue { projectExplorer.isExplored.isEmpty() }
        testProject.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        testProject.configurations.create("${TEST_SOURCE_SET}Implementation")
        addDependencyTo(
            testProject.dependencies,
            "${TEST_SOURCE_SET}Implementation",
            "$TEST_DEPENDENCY_GROUP:$TEST_DEPENDENCY_NAME:$TEST_DEPENDENCY_VERSION",
            configurationAction = Action { })
        projectExplorer.explore(testProject)
        assertEquals(1, projectExplorer.isExplored.size)
        assertEquals(1, projectExplorer.dependencySet.size)
        assertEquals(TEST_DEPENDENCY_GROUP, projectExplorer.dependencySet.first().group)
        assertEquals(TEST_DEPENDENCY_NAME, projectExplorer.dependencySet.first().name)
        assertEquals(TEST_DEPENDENCY_VERSION, projectExplorer.dependencySet.first().version)
    }

    @Test
    fun testExploreWithoutKotlinMultiplatformExtension() {
        assertTrue { projectExplorer.isExplored.isEmpty() }
        projectExplorer.explore(testProject)
        assertEquals(4, projectExplorer.isExplored.size)
        assertEquals(0, projectExplorer.dependencySet.size)
    }

    private fun generateTestProject(): Project {
        val projectBuilder = ProjectBuilder.builder()
        val testProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_1").withParent(testProject).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_2").withParent(testProject).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_3").withParent(testProject).build()
        return testProject
    }

    companion object {
        private const val TEST_PROJECT_NAME = "TEST_PROJECT_NAME"
        private const val TEST_SUBPROJECT_NAME = "TEST_SUBPROJECT_NAME"
        private const val TEST_EXTENSION_NAME = "TEST_EXTENSION_NAME"
        private const val TEST_SOURCE_SET = "commonMain"
        private const val TEST_DEPENDENCY_GROUP = "testgroup"
        private const val TEST_DEPENDENCY_NAME = "testdependency"
        private const val TEST_DEPENDENCY_VERSION = "1.0"
    }
}