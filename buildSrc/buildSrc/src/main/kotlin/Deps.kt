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
object Deps {

    private const val kotlinVersion = "1.5.10"

    val kotlin = Kotlin
    val kotlinx = Kotlinx
    val touchlab = TouchLab

    // Jacoco
    private const val jacocoVersion = "0.8.7"
    const val jacoco = "org.jacoco:org.jacoco.core:$jacocoVersion"

    object TouchLab {
        val stately = Stately

        object Stately {
            private const val statelyVersion = "1.1.7-a1"

            val isolate = Isolate

            object Isolate {
                const val common = "co.touchlab:stately-isolate:$statelyVersion"
            }
        }
    }

    object Kotlinx {
        val atomicfu = AtomicFu

        object AtomicFu {
            private const val atomicFuVersion = "0.16.1"
            const val plugin = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion"
            const val common = "org.jetbrains.kotlinx:atomicfu:$atomicFuVersion"
        }
    }

    object Kotlin {
        val test = Test()
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

        const val reflectJvm = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"

        class Test(private val name: String = "org.jetbrains.kotlin:kotlin-test:$kotlinVersion") :
            CharSequence by name {
            val annotationsCommon =
                "org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion"
            val js = "org.jetbrains.kotlin:kotlin-test-js:$kotlinVersion"
            val junit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"

            override fun toString() = name
        }
    }
}
