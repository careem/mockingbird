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

plugins{
    id("org.jetbrains.kotlin.multiplatform")
}

apply(from = "../../../utils.gradle")
val setupMultiplatformLibrary: Closure<Any> by ext
setupMultiplatformLibrary(project, false, false) // TODO extract JsPlugin from buildSrc to be used in other modules

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.atomicfu)
            }
        }
    }
}