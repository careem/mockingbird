package com.careem.mockingbird

import org.gradle.api.artifacts.Dependency
import org.gradle.api.invocation.Gradle
import java.io.File

fun Dependency.artifactPath(gradle: Gradle): String{
    return gradle.gradleUserHomeDir.absolutePath + File.separator + "caches/modules-2/files-2.1" + File.separator + this.group + File.separator + this.name + "-jvm" + File.separator + this.version
}