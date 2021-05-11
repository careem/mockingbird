plugins {
    `kotlin-dsl-base`
    `java-gradle-plugin`
}

buildscript {

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

}

gradlePlugin {
    plugins {
        register("plugin") {
            id = "com-careem-mockingbird"

            implementationClass = "com.careem.mockingbird.plugin.MockingBirdPlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))

    val kotlinVersion = "1.5.0"
    val kotlinPoetVersion = "1.8.0"

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-metadata:$kotlinPoetVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
