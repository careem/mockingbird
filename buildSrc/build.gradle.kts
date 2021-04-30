plugins {
    `kotlin-dsl`
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
    implementation(Deps.square.kotlinPoetMetadataSpecs)
    implementation(Deps.kotlinx.metadata)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDirs(
        "buildSrc/src/main/kotlin"
//        "../samples/src/commonMain/kotlin" // TODO fix

    )
    println("ROOT:${rootProject.allprojects}")
}
