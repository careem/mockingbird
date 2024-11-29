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
    alias(libs.plugins.gmazzo.buildconfig)
    id("maven-publish")
    signing
}

gradlePlugin {
    plugins {
        register("mockingbird") {
            id = "com.careem.mockingbird"
            implementationClass = "com.careem.mockingbird.MockingbirdPlugin"
            displayName = "mockingbird"
            description = "mockingbird"
        }
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${project.property("VERSION") as String}\"")
}

tasks.withType<JavaCompile>().configureEach {
    java {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    from("src/main/kotlin")
    archiveClassifier.set("sources")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(sourcesJar)
        }
    }
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.reflectjvm)
    implementation(libs.square.kotlinpoet)
    implementation(libs.square.kotlinpoet.metadata)
    implementation(libs.kotlinx.metadata.jvm)
    implementation(project(":mockingbird-processor"))
    implementation(project(":mockingbird"))

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk.mockk)
}

