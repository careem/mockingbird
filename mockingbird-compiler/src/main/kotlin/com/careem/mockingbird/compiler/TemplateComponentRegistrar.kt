package com.careem.mockingbird.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

@AutoService(ComponentRegistrar::class)
class TemplateComponentRegistrar(
  private val sourceGenFolder: String
) : ComponentRegistrar {

  @Suppress("unused") // Used by service loader
  constructor() : this(
    sourceGenFolder = "/home/carlo/IdeaProjects/kotlin-ir-plugin-template/kotlin-ir-plugin/build/generated/mocking"
  )

  override fun registerProjectComponents(
    project: MockProject,
    configuration: CompilerConfiguration
  ) {
    val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
    val sourceGenFolder = File(configuration.get(TemplateCommandLineProcessor.srcGenDirKey, sourceGenFolder))

    AnalysisHandlerExtension.registerExtension(project, TextCodegenExtension(messageCollector, sourceGenFolder))
  }
}


