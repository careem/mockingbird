/**
 *
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("mockingbird") {
            id = "com.careem.mockingbird"
            implementationClass = "com.careem.mockingbird.MockingbirdPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs("buildSrc/src/main/kotlin")
    println("ROOT:${rootProject.allprojects}")
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.square.kotlinpoet)
    implementation(libs.square.kotlinpoet.metadata)
    implementation(libs.square.kotlinpoet.metadata.specs)
    implementation(libs.kotlinx.metadatajvm)
}

//task pluginVersion {
//    def outputDir = file("gen")
//
//    inputs.property 'version', version
//    outputs.dir outputDir
//
//    doLast {
//        def versionFile = file("$outputDir/com/squareup/sqldelight/Version.kt")
//        versionFile.parentFile.mkdirs()
//        versionFile.text = """// Generated file. Do not edit!
//package com.squareup.sqldelight
//
//val VERSION = "${project.version}"
//"""
//    }
//}
//
//tasks.getByName('compileKotlin').dependsOn('pluginVersion')
//
//apply from: "$rootDir/gradle/gradle-mvn-push.gradle"

