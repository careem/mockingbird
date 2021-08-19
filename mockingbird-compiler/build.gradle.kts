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

apply(from = "../publishing.gradle")

gradlePlugin {
    plugins {
        register("mockingbird") {
            id = "com.careem.mockingbird"
            implementationClass = "com.careem.mockingbird.MockingbirdPlugin"
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.reflectjvm)
    implementation(libs.square.kotlinpoet)
    implementation(libs.square.kotlinpoet.metadata)
    implementation(libs.square.kotlinpoet.metadata.specs)
    implementation(libs.kotlinx.metadatajvm)
    implementation(project(":mockingbird"))
}

