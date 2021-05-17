package com.careem.mockingbird.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.OptionValue
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import kotlin.test.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.junit.Test
import java.io.File

class IrPluginTest {

    @Test
    fun `IR plugin success`() {
        val file1 = SourceFile.kotlin(
            "main.kt", file1
        )

        val result = compile(
            sourceFiles = listOf(file1)
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}

fun compile(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar = MockingBirdComponentRegistrar()
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        val commandLineProcessor = MockingBirdCommandLineProcessor()

        commandLineProcessors = listOf(commandLineProcessor)
        sources = sourceFiles
        useIR = false
        compilerPlugins = listOf(plugin)
        pluginOptions = listOf(
            PluginOption(
                commandLineProcessor.pluginId,
                MockingBirdCommandLineProcessor.OPTION_SRC_GEN_DIR,
                File(workingDir, "build/mockingbird").absolutePath
            )
        )
        inheritClassPath = true
    }.compile()


}


const val file1 = """
    package com.careem.test

    interface abc {
        fun a(i:Int, a: List<String>): String
        fun c()
    }

    interface anotherabc {
        fun a(i:Int, a: List<String>): String
        fun c():Unit
        fun b(a: Pair<String, Any> = Pair("a", 1)): Map<String, Int>
    }
"""