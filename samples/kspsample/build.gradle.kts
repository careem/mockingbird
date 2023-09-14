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
import groovy.lang.Closure

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp") version libs.versions.kspVersion.get()
    id("com.careem.mockingbird")
}

kotlin {
    ios()
    iosSimulatorArm64()
    jvm()
//    js(IR) {
//        nodejs()
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":common-sample"))
                implementation(project(":common:sample"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlin.test)
                implementation("com.careem.mockingbird:mockingbird")
            }
        }
    }
}

