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

class ProjectExplorerTest {

    private val sourceSetResolver = mockk<SourceSetResolver>()
    private lateinit var projectExplorer: ProjectExplorer

    @Before
    fun setup() {
        val testKotlinSourceSet = DefaultKotlinSourceSet(generateTestProject(), TEST_SOURCE_SET)
        every { sourceSetResolver.getSourceSetFromKmpExtension(any(), any()) } returns testKotlinSourceSet
        projectExplorer = ProjectExplorer(sourceSetResolver)
    }

    @Test
    fun testExploreProjectWithoutVisitRootProjectOnlyRetrieveSubprojectDependencies() {
        val projectBuilder = ProjectBuilder.builder()
        val rootProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        val subProject1 = projectBuilder.withName("${TEST_SUBPROJECT_NAME}_1").withParent(rootProject).build()
        val subProject2 = projectBuilder.withName("${TEST_DEPENDENCY_NAME}_2").withParent(rootProject).build()
        subProject1.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        subProject2.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        subProject1.configurations.create("${TEST_SOURCE_SET}Implementation")
        subProject2.configurations.create("${TEST_SOURCE_SET}Implementation")
        addImplementationDependencyToProject(subProject1, TEST_DEPENDENCY_NAME)
        addImplementationDependencyToProject(subProject1, "${TEST_DEPENDENCY_NAME}_2", dependencyGroup = "new_group")
        addImplementationDependencyToProject(subProject2, "${TEST_DEPENDENCY_NAME}_3")

        val dependencySet = projectExplorer.explore(subProject1)
        assertEquals(2, dependencySet.size)
        val dependencies = dependencySet.toList()
        assertEquals(TEST_DEPENDENCY_GROUP, dependencies[0].group)
        assertEquals(TEST_DEPENDENCY_NAME, dependencies[0].name)
        assertEquals(TEST_DEPENDENCY_VERSION, dependencies[0].version)
        assertEquals("new_group", dependencies[1].group)
        assertEquals("${TEST_DEPENDENCY_NAME}_2", dependencies[1].name)
        assertEquals(TEST_DEPENDENCY_VERSION, dependencies[1].version)
    }

    @Test
    fun testExploreProjectWithVisitRootProjectRetrieveAllDependencies() {
        val projectBuilder = ProjectBuilder.builder()
        val rootProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        val subProject1 = projectBuilder.withName("${TEST_SUBPROJECT_NAME}_1").withParent(rootProject).build()
        val subProject2 = projectBuilder.withName("${TEST_DEPENDENCY_NAME}_2").withParent(rootProject).build()
        subProject1.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        subProject2.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        subProject1.configurations.create("${TEST_SOURCE_SET}Implementation")
        subProject2.configurations.create("${TEST_SOURCE_SET}Implementation")
        addImplementationDependencyToProject(subProject1, TEST_DEPENDENCY_NAME)
        addImplementationDependencyToProject(subProject1, "${TEST_DEPENDENCY_NAME}_2", dependencyVersion = "2.0")
        addImplementationDependencyToProject(subProject2, "${TEST_DEPENDENCY_NAME}_3", dependencyVersion = "3.0")

        projectExplorer.visitRootProject(rootProject)
        val dependencySet = projectExplorer.explore(subProject1)
        assertEquals(3, dependencySet.size)
        val dependencies = dependencySet.toList()
        assertEquals(TEST_DEPENDENCY_GROUP, dependencies[0].group)
        assertEquals(TEST_DEPENDENCY_NAME, dependencies[0].name)
        assertEquals(TEST_DEPENDENCY_VERSION, dependencies[0].version)
        assertEquals(TEST_DEPENDENCY_GROUP, dependencies[1].group)
        assertEquals("${TEST_DEPENDENCY_NAME}_2", dependencies[1].name)
        assertEquals("2.0", dependencies[1].version)
        assertEquals(TEST_DEPENDENCY_GROUP, dependencies[2].group)
        assertEquals("${TEST_DEPENDENCY_NAME}_3", dependencies[2].name)
        assertEquals("3.0", dependencies[2].version)
    }

    @Test
    fun testExploreProjectWithKMPExtension() {
        val testProject = generateTestProject()
        testProject.extensions.add(TEST_EXTENSION_NAME, KotlinMultiplatformExtension::class.java)
        testProject.configurations.create("${TEST_SOURCE_SET}Implementation")
        addImplementationDependencyToProject(testProject, TEST_DEPENDENCY_NAME)

        val dependencySet = projectExplorer.explore(testProject)
        assertEquals(1, dependencySet.size)
        assertEquals(TEST_DEPENDENCY_GROUP, dependencySet.first().group)
        assertEquals(TEST_DEPENDENCY_NAME, dependencySet.first().name)
        assertEquals(TEST_DEPENDENCY_VERSION, dependencySet.first().version)
    }

    @Test
    fun testExploreProjectWithoutKotlinMultiplatformExtension() {
        val dependencySet = projectExplorer.explore(generateTestProject())
        assertEquals(0, dependencySet.size)
    }

    private fun generateTestProject(): Project {
        val projectBuilder = ProjectBuilder.builder()
        val testProject = projectBuilder.withName(TEST_PROJECT_NAME).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_1").withParent(testProject).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_2").withParent(testProject).build()
        projectBuilder.withName("${TEST_SUBPROJECT_NAME}_3").withParent(testProject).build()
        return testProject
    }

    private fun addImplementationDependencyToProject(
        project: Project,
        dependencyName: String,
        dependencyGroup: String = TEST_DEPENDENCY_GROUP,
        dependencyVersion: String = TEST_DEPENDENCY_VERSION
    ) {
        addDependencyTo(
            project.dependencies,
            "${TEST_SOURCE_SET}Implementation",
            "$dependencyGroup:${dependencyName}:$dependencyVersion",
            Action { })
    }

    companion object {
        private const val TEST_PROJECT_NAME = "testProject"
        private const val TEST_SUBPROJECT_NAME = "testSubproject"
        private const val TEST_EXTENSION_NAME = "testExtension"
        private const val TEST_SOURCE_SET = "commonMain"
        private const val TEST_DEPENDENCY_GROUP = "testgroup"
        private const val TEST_DEPENDENCY_NAME = "testdependency"
        private const val TEST_DEPENDENCY_VERSION = "1.0"
    }
}