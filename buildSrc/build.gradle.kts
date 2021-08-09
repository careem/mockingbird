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
    maven {
        url = uri(project.properties["careem_artifactory_url"] as String? ?: "")
        credentials {
            username = project.properties["careem_artifactory_username"] as String? ?: System.getenv("careem_artifactory_username")
            password = project.properties["careem_artifactory_api_key"] as String? ?: System.getenv("careem_artifactory_api_key")
        }
        content {
            includeGroup("com.careem.mockingbird")
        }
        mavenContent {
            releasesOnly()
        }
    }
}


dependencies {
    implementation(Deps.kotlin.plugin)
    implementation(Deps.kotlin.reflectJvm)
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
    implementation(Deps.square.kotlinPoetMetadataSpecs)
    implementation(Deps.kotlinx.metadata)
    implementation(Deps.careem.mockingbird.common) // FIXME avoid this dep but use project dep instead
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs(
        "buildSrc/src/main/kotlin"
    )
    println("ROOT:${rootProject.allprojects}")
}
