object Deps {

    const val kotlinVersion = "1.3.71"

    val kotlin = Kotlin
    val kotlinx = Kotlinx
    val touchlab = TouchLab
    val arrow = Arrow

    // Jacoco
    const val jacocoVersion = "0.8.5"
    const val jacoco = "org.jacoco:org.jacoco.core:$jacocoVersion"
    const val jvmTargetVersion = "1.8"

    private const val shadowVersion = "5.2.0"
    const val shadow = "com.github.johnrengelman.shadow:$shadowVersion"

    object TouchLab {
        val stately = Stately

        object Stately {
            private const val statelyVersion = "1.0.3-a4"

            val isolate = Isolate

            object Isolate {
                const val common = "co.touchlab:stately-isolate:$statelyVersion"
            }
        }
    }

    object Kotlinx {
        val atomicfu = AtomicFu

        object AtomicFu {
            private const val atomicFuVersion = "0.14.2"
            const val plugin = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion"

            const val common = "org.jetbrains.kotlinx:atomicfu-common:$atomicFuVersion"
            const val jvm = "org.jetbrains.kotlinx:atomicfu:$atomicFuVersion"
            const val native = "org.jetbrains.kotlinx:atomicfu-native:$atomicFuVersion"
        }
    }

    object Kotlin {
        val stdlib = Stdlib()
        val test = Test
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        const val kotlinCompilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"
        const val jvmPlugin = "org.jetbrains.kotlin.jvm"

        class Stdlib(
            private val name: String = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
        ) : CharSequence by name {
            val jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
            val common = "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"

            override fun toString(): String = name
        }

        object Test {
            const val common = "org.jetbrains.kotlin:kotlin-test-common:$kotlinVersion"
            const val junit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
            const val test = "org.jetbrains.kotlin:kotlin-test:$kotlinVersion"
            const val annotationsCommon =
                "org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion"
        }
    }

    object Arrow {
        const val arrowMetaVersion = "1.3.61-SNAPSHOT"
        const val compilerPlugin = "io.arrow-kt:compiler-plugin:$arrowMetaVersion"
        const val gradlePlugin = "io.arrow-kt:gradle-plugin:$arrowMetaVersion"
    }
}
