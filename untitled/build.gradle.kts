plugins {
    kotlin("jvm")
    id("com.careem.mockingbird")
}

group = "com.careem.mockingbird"
version = "1.8.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}
