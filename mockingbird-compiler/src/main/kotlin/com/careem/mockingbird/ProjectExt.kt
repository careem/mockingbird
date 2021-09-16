package com.careem.mockingbird

import org.gradle.api.Project
import java.io.File

/**
 * Unique identifier for the project
 */
fun Project.fullQualifier(): String{
    return "${this.group}:${this.name}"
}

/**
 * Path where current project will generate .class files
 */
fun Project.classPath(): String{
    return "${this.buildDir}/classes/kotlin/jvm/main"
}

/**
 * Path where the plugin will move dependencies .class files
 */
fun Project.thirdPartiesClassPath(): String {
    return this.buildDir.absolutePath + File.separator + "dependencies"
}

