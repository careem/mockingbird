plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
}


dependencies {
    implementation(Deps.kotlin.plugin)
}

kotlin {
    // Add Deps to compilation, so it will become available in main project
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}
