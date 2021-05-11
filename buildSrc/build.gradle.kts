import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    `kotlin-dsl`
    kotlin("kapt") version "1.5.0"
}

repositories {
    mavenCentral()
    google()
    jcenter()
    gradlePluginPortal()
}


dependencies {
    implementation(Deps.kotlin.plugin)
    implementation(Deps.kotlin.reflectJvm)
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs(
        "buildSrc/src/main/kotlin"
//        "../samples/src/commonMain/kotlin" // TODO fix

    )
    println("ROOT:${rootProject.allprojects}")
}
