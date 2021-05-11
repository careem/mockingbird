package com.careem.mockingbird.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Test

class IrPluginTest {
  @Test
  fun `IR plugin success`() {
    val file1 = SourceFile.kotlin(
      "main.kt", """
    import kotlin.collections.List

    interface abc {
        fun a(i:Int, a: List<String>): String
        fun c()
    }

    interface anotherabc {
        fun a(i:Int, a: List<String>): String
        fun c():Unit
        fun b(a: Pair<String, Any> = Pair("a", 1)): Map<String, Int>
    }
fun main() {
//  println(debug())
}
"""
    )

    val file2 = SourceFile.kotlin(
      "main2.kt", """
fun main2() {
//  println(debug())
}

fun debug2() = "Hello, World!2"
"""
    )

    val result = compile(
      sourceFiles = listOf(file1, file2)
    )
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
  }
}

fun compile(
  sourceFiles: List<SourceFile>,
  plugin: ComponentRegistrar = TemplateComponentRegistrar()
): KotlinCompilation.Result {
  return KotlinCompilation().apply {
    sources = sourceFiles
    useIR = false
    compilerPlugins = listOf(plugin)
    inheritClassPath = true
  }.compile()
}

fun compile(
  sourceFile: SourceFile,
  plugin: ComponentRegistrar = TemplateComponentRegistrar()
): KotlinCompilation.Result {
  return compile(listOf(sourceFile), plugin)
}
