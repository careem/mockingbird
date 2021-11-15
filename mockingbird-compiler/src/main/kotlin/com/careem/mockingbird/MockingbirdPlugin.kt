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

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

private const val EXTENSION_NAME = "mockingBird"


@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockingbirdPlugin : Plugin<Project> {

    private lateinit var classLoader: ClassLoaderWrapper
    private lateinit var functionsMiner: FunctionsMiner
    private lateinit var projectExplorer: ProjectExplorer
    private lateinit var mockGenerator: MockGenerator
    private val logger: Logger = Logging.getLogger(this::class.java)

    private fun setupDependencies(target: Project) {
        classLoader = ClassLoaderWrapper(projectExplorer, target)
        functionsMiner = FunctionsMiner(classLoader)
        mockGenerator = MockGenerator(classLoader, functionsMiner)
    }

    override fun apply(target: Project) {
        val sourceSetResolver = SourceSetResolver()
        projectExplorer = ProjectExplorer(sourceSetResolver)
        try {
            configureSourceSets(target)

            target.extensions.add<MockingbirdPluginExtension>(
                EXTENSION_NAME, MockingbirdPluginExtensionImpl(target.objects)
            )

            target.task(GradleTasks.GENERATE_MOCKS) {
                dependsOn(target.tasks.getByName(GradleTasks.ASSEMBLE))
                doLast {
                    generateMocks(target)
                }
            }

            target.tasks.getByName(GradleTasks.ALL_TESTS) {
                dependsOn(target.tasks.getByName(GradleTasks.GENERATE_MOCKS))
            }

            projectExplorer.visitRootProject(target.rootProject)
            // Add test dependencies for classes that need to be mocked
            target.gradle.projectsEvaluated {
                val dependencySet = projectExplorer.explore(target)
                target.extensions.getByType(KotlinMultiplatformExtension::class.java).run {
                    sourceSets.getByName("commonTest") {
                        dependencies {
                            dependencySet.forEach { implementation(it) }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            // Useful to debug
            e.printStackTrace()
            throw e
        }
    }

    private fun generateMocks(target: Project) {
        setupDependencies(target)

        val pluginExtensions = target.extensions[EXTENSION_NAME] as MockingbirdPluginExtensionImpl
        logger.info("Mocking: ${pluginExtensions.generateMocksFor}")
        val outputDir =
            File(target.buildDir.absolutePath + File.separator + "generated" + File.separator + "mockingbird")
        outputDir.mkdirs()

        pluginExtensions.generateMocksFor
            .map { classLoader.loadClass(it).toImmutableKmClass() }
            .let { generateClasses(it, outputDir) }
    }

    private fun configureSourceSets(target: Project) {
        // TODO check if kmpProject before this
        target.extensions.configure(KotlinMultiplatformExtension::class.java) {
            sourceSets.getByName("commonTest") {
                kotlin.srcDir("build/generated/mockingbird")
            }
        }
    }


    private fun generateClasses(classNames: List<ImmutableKmClass>, outputDir: File) {
        for (kmClass in classNames) {
            mockGenerator.createClass(kmClass).writeTo(outputDir)
        }
    }
}




