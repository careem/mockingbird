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
    implementation(Deps.kotlin.plugin)
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
    implementation(Deps.square.kotlinPoetMetadataSpecs)
    implementation(Deps.kotlinx.metadata)
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

