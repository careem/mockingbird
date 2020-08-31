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
    implementation(Deps.square.kotlinPoet)
    implementation(Deps.square.kotlinPoetMetadata)
    implementation(Deps.square.kotlinPoetMetadataSpecs)
    implementation(Deps.kotlinx.metadata)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}
