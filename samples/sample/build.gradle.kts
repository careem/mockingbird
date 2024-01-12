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
    id("com.careem.mockingbird")
}

kotlin {
    iosX64()
    iosArm64()
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
                implementation("com.careem.mockingbird:mockingbird")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

configure<com.careem.mockingbird.MockingbirdPluginExtension> {
    generateMocksFor = listOf(
        "com.careem.mockingbird.sample.JavaTypes",
        "com.careem.mockingbird.sample.InterfaceWithGenerics",
        "com.careem.mockingbird.sample.PippoSample",
        "com.careem.mockingbird.sample.InternalSampleInterface",
        "com.careem.mockingbird.sample.LambdaSample",
        "com.careem.mockingbird.sample.Mock1",
        "com.careem.mockingbird.sample.MockWithExternalDependencies",
        "com.careem.mockingbird.common.sample.ExternalContract",
        "com.careem.mockingbird.sample.OuterInterface",
        "com.careem.mockingbird.sample.MultipleGetterProperties",
        "com.careem.mockingbird.common.sample.ExternalDep"
    )
}

