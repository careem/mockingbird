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
pluginManagement {
    apply(from = "properties.gradle")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }

    resolutionStrategy {
        val publishVersion = extra["VERSION"] as String
        eachPlugin {
            if (requested.id.id == "com.careem.mockingbird") {
                useVersion(publishVersion)
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../versions.toml"))
        }
    }
}

include(":sample")
include(":kspsample")
include(":common-sample")
include(":common:sample")

includeBuild("..") {
    dependencySubstitution {
        substitute(module("com.careem.mockingbird:mockingbird")).using(project(":mockingbird"))
        substitute(module("com.careem.mockingbird:mockingbird-compiler")).using(project(":mockingbird-compiler"))
        substitute(module("com.careem.mockingbird:mockingbird-processor")).using(project(":mockingbird-processor"))
    }
}
