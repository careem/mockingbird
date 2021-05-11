
package com.careem.mockingbird.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*
import java.io.File

class MockingBirdPlugin : KotlinCompilerPluginSupportPlugin {
  override fun apply(target: Project): Unit = with(target) {
//    extensions.create("mockingbird", MockingBirdPluginExtension::class.java)
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun getCompilerPluginId(): String = "mocking-bird-plugin-compiler"

  override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
    groupId = "com.careem.mockingbird",
    artifactId = "mocking-bird-plugin",
    version = "1"
  )

  override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
    groupId = "com.careem.mockingbird",
    artifactId = "mocking-bird-plugin-native",
    version = "1"
  )

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {

    // TODO run only if running tests?

    val project = kotlinCompilation.target.project
    val extension = project.extensions.getByType(MockingBirdPluginExtension::class.java)

    // Notice that we use the name of the Kotlin compilation as a directory name. Generated code
    // for this specific compile task will be included in the task output. The output of different
    // compile tasks shouldn't be mixed.
    val srcGenDir = File(
      project.buildDir,
      "mockingbird${File.separator}src-gen-${kotlinCompilation.name}"
    )

    return project.provider {
      listOf(
        FilesSubpluginOption(key = "src-gen-dir", files = listOf(srcGenDir)),
      )
    }
  }
}
