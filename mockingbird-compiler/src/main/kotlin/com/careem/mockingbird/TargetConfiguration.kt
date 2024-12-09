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
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal class TargetConfigurationFactory {
    fun get(target: KotlinTarget): TargetConfiguration = when (target.platformType) {
        KotlinPlatformType.androidJvm -> AndroidTargetConfiguration(target)
        KotlinPlatformType.jvm -> JvmTargetConfiguration(target)
        else -> throw IllegalArgumentException("Unsupported target: ${target.name}")
    }
}

internal interface TargetConfiguration {
    fun getKspConfiguration(): String
    fun getSrcDir(project: Project): List<String>
    fun getSourceSet(): String
    fun getKspTask(): String
}

internal class AndroidTargetConfiguration(private val target: KotlinTarget) : TargetConfiguration {
    override fun getKspConfiguration(): String =
        "ksp${target.name.replaceFirstChar { it.titlecase() }}TestDebug"

    override fun getSrcDir(project: Project): List<String> = listOf(
        "${project.layout.buildDirectory.asFile.get()}/generated/ksp/${target.name}/${target.name}UnitTestDebug/kotlin",
        "${project.layout.buildDirectory.asFile.get()}/generated/ksp/${target.name}/${target.name}DebugUnitTest/kotlin",
        "${project.layout.buildDirectory.asFile.get()}/generated/ksp/${target.name}/${target.name}TestDebug/kotlin"
    )

    override fun getKspTask(): String =
        "kspDebugUnitTestKotlin${target.name.replaceFirstChar { it.titlecase() }}"

    override fun getSourceSet(): String = "androidUnitTestDebug"
}

internal class JvmTargetConfiguration(private val target: KotlinTarget) : TargetConfiguration {
    override fun getKspConfiguration(): String =
        "ksp${target.name.replaceFirstChar { it.titlecase() }}Test"

    override fun getSrcDir(project: Project): List<String> = listOf(
        "${project.layout.buildDirectory.asFile.get()}/generated/ksp/${target.name}/${target.name}Test/kotlin"
    )

    override fun getKspTask(): String =
        "kspTestKotlin${target.name.replaceFirstChar { it.titlecase() }}"

    override fun getSourceSet(): String = "jvmTest"
}