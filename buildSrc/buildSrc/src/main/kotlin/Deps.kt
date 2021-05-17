object Deps {

    private const val kotlinVersion = "1.5.0"

    val kotlin = Kotlin
    val kotlinx = Kotlinx
    val touchlab = TouchLab
    val square = Square

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
        val metadata = Metadata()
        val metadataVersion = "0.1.0"

        object AtomicFu {
            private const val atomicFuVersion = "0.14.4"
            const val plugin = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion"
            const val common = "org.jetbrains.kotlinx:atomicfu:$atomicFuVersion"
        }

        class Metadata(
            private val name: String = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:$metadataVersion"
        ) : CharSequence by name {

            override fun toString(): String = name
        }
    }

    object Square {
        private val version = "1.8.0"

        val kotlinPoet = "com.squareup:kotlinpoet:$version"
        val kotlinPoetMetadata = "com.squareup:kotlinpoet-metadata:$version"
        val kotlinPoetMetadataSpecs = "com.squareup:kotlinpoet-metadata-specs:$version"
    }

    object Kotlin {
        val test = Test()
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        const val pluginApi = "org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinVersion"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"

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

    object Google {
        const val autoservice = "com.google.auto.service:auto-service:1.0"
    }
}
