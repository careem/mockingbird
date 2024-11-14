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

import com.careem.mockingbird.mockingbird_compiler.BuildConfig
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

class MockingbirdPluginKspDelegate {
    private val targetConfigurationFactory: TargetConfigurationFactory by lazy { TargetConfigurationFactory() }

    fun apply(target: Project) {
        target.afterEvaluate {
            if (!hasKspPlugin(this)) {
                logger.info("KSP plugin not found, falling back to legacy plugin")
                return@afterEvaluate
            }

            // TODO revisit this whole logic once Ksp will be able to generate code for commonTest ( not the case today see: https://github.com/google/ksp/issues/567 )
            // The following code will workaround the current ksp limitations doing the follow:
            // 1. To avoid running ksp for each target the plugin will run ksp for a single target jvm will be prefered if the target is available otherwise it will pick the first target
            // 2. Since current multiplatform ksp implementation can target specific targets, mocks generated will not be resolved
            //    in commonTest. The plugin will add this the code generated at point 1 as source set for common test so that
            //    this code will be available for each platform and resolvable by the IDE

            val kotlin: KotlinProjectExtension = project.kotlinExtension

            val jvmTarget = kotlin.targets.firstOrNull { it.platformType == KotlinPlatformType.jvm }
                ?: kotlin.targets.firstOrNull { it.platformType == KotlinPlatformType.androidJvm }
                ?: throw GradleException("Could not find JVM or Android target")

            val targetConfiguration = targetConfigurationFactory.get(jvmTarget)

            addKSPDependency(project, targetConfiguration.getKspConfiguration())

            project.afterEvaluate {
                val commonTest = kotlin.sourceSets.getByName("commonTest")

                addRuntimeDependencies(commonTest)

                // Adding ksp generated code as source set for commonTest
                commonTest.kotlin.srcDirs(targetConfiguration.getSrcDir(this))

                // Turns out that the simple fact of calling `project.tasks.withType<KotlinCompile<*>>()`
                // breaks KSP if it isn't set in a deep level of afterEvaluate
                afterEvaluate {
                    afterEvaluate {
                        // Adding KSP JVM as a dependency to all Kotlin compilations
                        project.tasks.withType<KotlinCompile<*>>().all {
                            if (name.startsWith("compile") && name.contains("TestKotlin")) {
                                dependsOn(targetConfiguration.getKspTask())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addKSPDependency(project: Project, kspConfiguration: String) {
        project.dependencies {
            add(kspConfiguration, "com.careem.mockingbird:mockingbird-processor:${BuildConfig.VERSION}")
        }
    }

    private fun addRuntimeDependencies(sourceSet: KotlinSourceSet) {
        sourceSet.dependencies {
            implementation("com.careem.mockingbird:mockingbird:${BuildConfig.VERSION}")
        }
    }

    private fun hasKspPlugin(target: Project): Boolean =
        target.plugins.findPlugin("com.google.devtools.ksp") != null
}

private val KotlinProjectExtension.targets: Iterable<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOf(this.target)
        is KotlinMultiplatformExtension -> targets
        else -> error("Unexpected 'kotlin' extension $this")
    }
