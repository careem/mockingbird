
plugins {
    `kotlin-dsl`
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
    google()
    jcenter()
    gradlePluginPortal()
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs("buildSrc/src/main/kotlin")
    println("ROOT:${rootProject.allprojects}")
}

dependencies {
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    kapt("com.google.auto.service:auto-service:1.0")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.4")
}



tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
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

