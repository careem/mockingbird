package com.careem.mockingbird.compiler

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

class MockingBirdGeneratorExtension(
  private val messageCollector: MessageCollector,
  private val sourceGenFolder: File
) : AnalysisHandlerExtension {

  override fun doAnalysis(
    project: Project,
    module: ModuleDescriptor,
    projectContext: ProjectContext,
    files: Collection<KtFile>,
    bindingTrace: BindingTrace,
    componentProvider: ComponentProvider
  ): AnalysisResult? {
    messageCollector.report(CompilerMessageSeverity.ERROR, "mockingbird We are in doAnalysis ${files.size}")
    return null
  }


  override fun analysisCompleted(
    project: Project,
    module: ModuleDescriptor,
    bindingTrace: BindingTrace,
    files: Collection<KtFile>
  ): AnalysisResult? {

    messageCollector.report(CompilerMessageSeverity.ERROR, "mockingbird We are in analysisCompleted ${files.size}")

    val generator = MockingBirdGenerator(sourceGenFolder, bindingTrace.bindingContext, module, messageCollector)

    files.forEach {
      messageCollector.report(CompilerMessageSeverity.INFO, "Generating Mock for file ${it.name}")
      generator.generateMockClassFor(it)
    }

    return null
  }


}
