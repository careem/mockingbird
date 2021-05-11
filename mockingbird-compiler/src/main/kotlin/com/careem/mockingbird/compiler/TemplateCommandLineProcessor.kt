package com.careem.mockingbird.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
class TemplateCommandLineProcessor : CommandLineProcessor {
  companion object {
    const val OPTION_SRC_GEN_DIR = "src-gen-dir"

    val srcGenDirKey = CompilerConfigurationKey<String>(OPTION_SRC_GEN_DIR)
  }

  override val pluginId: String = "mocking-bird-plugin"

  override val pluginOptions: Collection<CliOption> = listOf(
    CliOption(
      optionName = OPTION_SRC_GEN_DIR,
      valueDescription = "src-gen-dir",
      description = "src-gen-dir path",
      required = true
    )
  )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
  ) {
    return when (option.optionName) {
      OPTION_SRC_GEN_DIR -> configuration.put(srcGenDirKey, value)
      else -> throw IllegalArgumentException("Unexpected config option ${option.optionName}")
    }
  }
}
