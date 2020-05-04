/**
 *
 * Copied from Reaktive build config https://github.com/badoo/Reaktive
 *
 * Should be changed soon just POC
 *
 */
object Deps {

    private const val kotlinVersion = "1.3.71"
    private const val coroutinesVersion = "1.3.5"
    private const val androidGradlePlugin = "3.6.3"
    private const val appCompatVersion = "1.1.0"
    private const val constraintLayoutVersion = "1.1.3"
    private const val workManagerVersion = "2.3.0"
    private const val junitAndroidxVersion = "1.1.1"
    private const val testRunnerVersion = "1.0.2"
    private const val espressoVersion = "3.2.0"
    private const val mockkVersion = "1.9.3"
    private const val reaktiveVersion = "1.1.12"
    private const val ktorVersion = "1.3.2"

    val kotlin = Kotlin
    val stately = Stately
    val kotlinx = Kotlinx
    val android = Android
    val buildKonfig = BuildKonfig
    val sqlDelight = SqlDelight
    val mockk = Mockk()
    val reaktive = Reaktive
    val ktor = Ktor

    // Shadow
    private const val shadowVersion = "5.1.0"
    const val shadow = "com.github.jengelman.gradle.plugins:shadow:$shadowVersion"

    // Jacoco
    const val jacocoVersion = "0.8.5"
    const val jacoco = "org.jacoco:org.jacoco.core:$jacocoVersion"

    // Robolectric
    private const val robolectricVersion = "4.3.1"
    const val robolectric = "org.robolectric:robolectric:$robolectricVersion"

    object Kotlin {
        val stdlib = Stdlib()
        val test = Test
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"

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

    object Kotlinx {
        val coroutines = Coroutines
        val serialization = Serialization
        val atomicfu = AtomicFu

        object Coroutines {
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
            const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
            val core = Core()

            class Core(
                private val name: String = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
            ) : CharSequence by name {
                val common =
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion"
                val native =
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion"

                override fun toString() = name
            }
        }

        object Serialization {
            const val plugin = "org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion"
            val runtime = Runtime()

            private const val serializationVersion = "0.20.0"

            class Runtime(
                private val name: String = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion"
            ) : CharSequence by name {
                val common =
                    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion"
                val native =
                    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationVersion"

                override fun toString() = name
            }
        }

        object AtomicFu {
            private const val atomicFuVersion = "0.14.2"
            const val plugin = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicFuVersion"

            const val common = "org.jetbrains.kotlinx:atomicfu-common:$atomicFuVersion"
            const val jvm = "org.jetbrains.kotlinx:atomicfu:$atomicFuVersion"
            const val native = "org.jetbrains.kotlinx:atomicfu-native:$atomicFuVersion"
        }
    }

    object SqlDelight {
        private const val sqlDelightVersion = "1.3.0"
        const val plugin = "com.squareup.sqldelight:gradle-plugin:$sqlDelightVersion"
        const val runtime = "com.squareup.sqldelight:runtime:$sqlDelightVersion"
        val driver = Driver

        object Driver {
            const val native = "com.squareup.sqldelight:native-driver:$sqlDelightVersion"
            const val sqlite = "com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion"
            const val android = "com.squareup.sqldelight:android-driver:$sqlDelightVersion"
        }
    }

    object Stately {
        private const val statelyVersion = "1.0.3-a4"

        val isocollections = IsoCollections
        val isolate = Isolate

        object Isolate {
            const val common = "co.touchlab:stately-isolate:$statelyVersion"
        }

        object IsoCollections {
            const val common = "co.touchlab:stately-iso-collections:$statelyVersion"
        }
    }

    object Android {
        const val compileSdkVersion = 29
        const val minSdkVersion = 21
        const val targetSdkVersion = 29
        const val plugin = "com.android.tools.build:gradle:$androidGradlePlugin"
        const val testRunner = "com.android.support.test:runner:$testRunnerVersion"
        val androidx = Androidx

        object Androidx {
            const val appcompat = "androidx.appcompat:appcompat:$appCompatVersion"
            const val constraintLayout =
                "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
            const val workManager = "androidx.work:work-runtime:$workManagerVersion"
            const val junitAndroidx = "androidx.test.ext:junit:$junitAndroidxVersion"
            const val espressoCore = "androidx.test.espresso:espresso-core:$espressoVersion"

            val core = Core

            object Core {
                private const val androidxKtxVersion = "1.2.0"
                const val ktx = "androidx.core:core-ktx:$androidxKtxVersion"
            }
        }
    }

    object BuildKonfig {
        private const val buildkonfigVersion = "0.5.1"
        const val plugin =
            "com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:$buildkonfigVersion"
    }

    class Mockk(
        private val name: String = "io.mockk:mockk:$mockkVersion"
    ) : CharSequence by name {
        val common = "io.mockk:mockk-common:$mockkVersion"
        override fun toString() = name
    }

    object Reaktive {
        const val reaktive = "com.badoo.reaktive:reaktive:$reaktiveVersion"
        const val coroutinesInterop = "com.badoo.reaktive:coroutines-interop:$reaktiveVersion"
        const val testing = "com.badoo.reaktive:reaktive-testing:$reaktiveVersion"
    }

    object Ktor {
        val client = Client

        object Client {
            const val core = "io.ktor:ktor-client-core:$ktorVersion"
            const val android = "io.ktor:ktor-client-android:$ktorVersion"
            const val okhttp = "io.ktor:ktor-client-okhttp:$ktorVersion"

            const val ios = "io.ktor:ktor-client-ios:$ktorVersion"

            val serialization = Serialization()
            val logging = Logging()

            class Serialization(
                private val name: String = "io.ktor:ktor-client-serialization:$ktorVersion"
            ) : CharSequence by name {
                val jvm = "io.ktor:ktor-client-serialization-jvm:$ktorVersion"
                val native = "io.ktor:ktor-client-serialization-native:$ktorVersion"
                override fun toString() = name
            }

            class Logging(
                private val name: String = "io.ktor:ktor-client-logging:$ktorVersion"
            ) : CharSequence by name {
                val jvm = "io.ktor:ktor-client-logging-jvm:$ktorVersion"
                val native = "io.ktor:ktor-client-logging-native:$ktorVersion"
                override fun toString() = name
            }
        }
    }
}
