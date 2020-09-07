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

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}


dependencies {
    implementation(Deps.kotlin.plugin)
    implementation(Deps.kotlin.reflect)
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
    implementation(Deps.square.kotlinPoetMetadataSpecs)
    implementation(Deps.kotlinx.metadata)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs(
        "buildSrc/src/main/kotlin"
//        "../samples/src/commonMain/kotlin" // TODO fix

    )
    println("ROOT:${rootProject.allprojects}")
}
