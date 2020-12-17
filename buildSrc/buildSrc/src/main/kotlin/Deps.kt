object Deps {

    private const val kotlinVersion = "1.4.21"

    val kotlin = Kotlin
    val kotlinx = Kotlinx
    val touchlab = TouchLab

    // Jacoco
    private const val jacocoVersion = "0.8.5"
    const val jacoco = "org.jacoco:org.jacoco.core:$jacocoVersion"

    object TouchLab {
        val stately = Stately

        object Stately {
            private const val statelyVersion = "1.1.0-a1"

            val isolate = Isolate

            object Isolate {
                const val common = "co.touchlab:stately-isolate:$statelyVersion"
            }
        }
    }

    object Kotlinx {
        val atomicfu = AtomicFu

        object AtomicFu {
            private const val atomicFuVersion = "0.14.4"
            const val plugin = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion"
            const val common = "org.jetbrains.kotlinx:atomicfu:$atomicFuVersion"
        }
    }

    object Kotlin {
        val test = Test
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

        object Test {
            const val common = "org.jetbrains.kotlin:kotlin-test:$kotlinVersion"
            const val junit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
            const val js = "org.jetbrains.kotlin:kotlin-test-js:$kotlinVersion"
            const val test = "org.jetbrains.kotlin:kotlin-test:$kotlinVersion"
            const val annotationsCommon =
                "org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion"
        }
    }
}
