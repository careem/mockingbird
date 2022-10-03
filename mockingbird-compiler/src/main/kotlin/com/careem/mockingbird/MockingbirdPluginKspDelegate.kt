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
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MockingbirdPluginKspDelegate {
    fun apply(target: Project) {
        target.afterEvaluate {
            if (hasKspPlugin(this)) {
                // TODO revisit this whole logic once Ksp will be able to generate code for commonTest ( not the case today see: https://github.com/google/ksp/issues/567 )
                // The following code will workaround the current ksp limitations doing the follow:
                // 1. To avoid running ksp for each target the plugin will run ksp for a single target jvm will be prefered if the target is available otherwise it will pick the first target
                // 2. Since current multiplatform ksp implementation can target specific targets, mocks generated will not be resolved
                //    in commonTest. The plugin will add this the code generated at point 1 as source set for common test so that
                //    this code will be available for each platform and resolvable by the IDE
                target.extensions.configure(KotlinMultiplatformExtension::class.java) {
                    val firstTargetName = targets.filter { it.targetName != "metadata" }.first().targetName
                    val selectedTargetName =
                        targets.filter { it.targetName == "jvm" }.firstOrNull()?.targetName ?: firstTargetName
                    sourceSets.getByName("commonTest") {
                        kotlin.srcDir("build/generated/ksp/$selectedTargetName/${selectedTargetName}Test/kotlin")
                    }
                    target.dependencies {
                        "ksp${selectedTargetName.capitalized()}Test"("com.careem.mockingbird:mockingbird-processor:${BuildConfig.VERSION}")
                    }
                    tasks.forEach { task ->
                        if (task.name.contains("Test") && (task is KotlinCompile<*>)) {
                            task.dependsOn("kspTestKotlin${selectedTargetName.capitalized()}")
                        }
                    }
                }
            }
        }
    }

    private fun hasKspPlugin(target: Project): Boolean {
        return target.plugins.findPlugin("com.google.devtools.ksp") != null
    }
}