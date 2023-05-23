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
    kotlin("jvm")
    id("maven-publish")
}

apply(from = "../publishing.gradle")

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

val javaTargetVersion = JavaVersion.VERSION_11
val kotlinTargetVersion = "1.6"

tasks.withType<JavaCompile>().configureEach {
    java {
        sourceCompatibility = javaTargetVersion
        targetCompatibility = javaTargetVersion
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn"
        )
        apiVersion = kotlinTargetVersion
        languageVersion = kotlinTargetVersion
        jvmTarget = javaTargetVersion.majorVersion
    }
}

publishing {
    publications {
        create<MavenPublication>("mockingbird-processor") {
            from(components["java"])
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    from("src/main/kotlin")
    archiveClassifier.set("sources")
}

dependencies {
    implementation(libs.google.ksp)
    implementation(libs.square.kotlinpoet.ksp)
    implementation(project(":mockingbird"))
}

