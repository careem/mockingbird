package com.careem.mockingbird

import com.careem.mockingbird.mockingbird_compiler.BuildConfig
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class MockingbirdPluginKspDelegate {
    fun apply(target: Project) {
        target.afterEvaluate {
            if (hasKspPlugin(this)) {
                // TODO revisit this whole block once Ksp will be able to generate code for commonTest ( not the case today see: https://github.com/google/ksp/issues/567 )
                // This leads to following limitations:
                // 1. It is not possible to mock on target specific tests the same object, otherwise there will be a file already exists problme since the output of commonTest and jvmTest for exampel are on the same folder
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