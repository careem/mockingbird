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
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    signing
}

kotlin {
    ios()
    iosSimulatorArm64()
    jvm()
    js(IR) {
        nodejs()
    }

    explicitApi()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.atomicfu)
                implementation(libs.touchlab.stately.isolate)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlin.test)
            }
        }
        val iosMain by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}