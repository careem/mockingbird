/**
 *
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("maven-publish")
    signing
}
apply(from = "jacoco.gradle")

val prop = java.util.Properties().apply {
    val localProp = File(rootProject.rootDir, "local.properties")
    if (localProp.exists()) {
        load(java.io.FileInputStream(localProp))
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set((prop["ossrhUsername"] ?: System.getenv("OSSRH_USERNAME") ?: "not-set") as String)
            password.set((prop["ossrhPassword"] ?: System.getenv("OSSRH_PASSWORD") ?: "not-set") as String)
            stagingProfileId.set((prop["sonatypeStagingProfileId"] ?: System.getenv("SONATYPE_STAGING_PROFILE_ID")) as String?)
        }
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.kotlinx.atomicfu.gradle)
        classpath(libs.jacoco.jacoco)
        classpath(libs.kotlin.gradle)
    }
}

allprojects {
    group = findProperty("GROUP") as String
    version = findProperty("VERSION") as String

    repositories {
        mavenCentral()
        google()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
            apiVersion = libs.versions.kotlinTarget.get()
            languageVersion = libs.versions.kotlinTarget.get()
            jvmTarget = libs.versions.jvmTarget.get()

            allWarningsAsErrors = true
        }
    }

    tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(tasks.withType(Sign::class.java))
    }
}

subprojects {
    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications {
                withType<MavenPublication> {

                    artifact(tasks.getByName("javadocJar"))

                    pom {
                        name.set("mockingbird")
                        description.set("A Koltin multiplatform library that provides an easier way to mock and write unit tests for a multiplatform project")
                        url.set("https://github.com/careem/mockingbird")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                organization.set("Careem Inc")
                                organizationUrl.set("https://careem.com")
                            }
                        }
                        scm {
                            url.set("https://github.com/careem/mockingbird")
                        }
                    }
                }
            }

        }

    }

    pluginManager.withPlugin("signing") {
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                (prop["signing.keyId"] ?: System.getenv("SIGNING_KEY_ID") ?: "not-set").toString(),
                (prop["signing.key"] ?: System.getenv("SIGNING_KEY") ?: "not-set").toString(),
                (prop["signing.password"] ?: System.getenv("SIGNING_PASSWORD") ?: "not-set").toString()
            )
            sign(publishing.publications)
        }
    }
}