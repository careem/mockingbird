import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass

/**
 * How to run
 * 1) Clear cache and build folders
 * 2) Comment out plugin apply form sample
 * 3) Build
 * 4) Uncomment plugin apply
 * 5) execute plugin
 */

@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockingbirdPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        try {
            // TODO delete file before build
            configureSourceSets(target)


            // TODO this requires project build already executed
            val file =
                File("/Users/marcosignoretto/Documents/careem/mockingbird/samples/build/classes/kotlin/jvm/main")

            // Convert File to a URL
            val url = file.toURI().toURL()          // file:/c:/myclasses/
            val urls = arrayOf(url)
            // Set kotlin class loader as parent in this way kotlin metadata will be loaded
            val cl = URLClassLoader(urls, Thread.currentThread().contextClassLoader)
            Thread.currentThread().contextClassLoader = cl
            val externalClass = cl.loadClass("com.careem.mockingbird.samples.PippoSample")

            val kmClasses = listOf(externalClass.toImmutableKmClass())
            generateClasses(target, kmClasses)
        } catch (e: Exception) {
            // Useful to debug
            e.printStackTrace()
            throw e
        }
    }

    private fun configureSourceSets(target: Project) {
        // TODO check if kmpProject before this
        target.extensions.configure(KotlinMultiplatformExtension::class.java) {
            sourceSets.getByName("commonTest") {
                kotlin.srcDir("build/generated/mockingbird")
            }
        }
    }

    private fun generateClasses(project: Project, classNames: List<ImmutableKmClass>) {
        for (kmClass in classNames) {
            generateMockClassFor(project, kmClass)
        }
    }

    private fun generateMockClassFor(project: Project, kmClass: ImmutableKmClass) {
        val classToMock = Thread.currentThread().contextClassLoader.loadClass(
            kmClass.name.replace(
                "/",
                "."
            )
        )
        val simpleName = kmClass.name.substringAfterLast("/")
        val outputDir =
            File(project.buildDir.absolutePath + File.separator + "generated" + File.separator + "mockingbird")
        outputDir.mkdirs()

        val packageName = "com.careem.mockingbird"

        // TODO fix package name
        println("Generating mocks for $simpleName")
        val mockClassBuilder = TypeSpec.classBuilder("${simpleName}Mock")
            .addType(kmClass.buildMethodObject())
            .addType(kmClass.buildArgObject())
//            .addSuperinterface(Mock::class) // TODO fix this
            .addSuperinterface(classToMock) // TODO check if interface or generic open class

//                    .primaryConstructor(
//                        FunSpec.constructorBuilder()
//                            .addParameter("name", String::class)
//                            .build()
//                    )
//                    .addProperty(
//                        PropertySpec.builder("name", String::class)
//                            .initializer("name")
//                            .build()
//                    )
//            .addFunction(
//                FunSpec.builder("greet")
//                    .addStatement("println(%P)", "Hello, \$name")
//                    .build()
//            )
//
//            .build()
        for (function in kmClass.functions) {
            this.mockFunction(mockClassBuilder, function, isUnitFunction(function))
        }
        // TODO support properties
        val file = FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()

        file.writeTo(outputDir)
    }

    private fun ImmutableKmClass.buildMethodObject(): TypeSpec {
        println("===> Methods")
        val methodObjectBuilder = TypeSpec.objectBuilder(MockingbirdPlugin.METHOD)
        for (function in this.functions) {
            methodObjectBuilder.addProperty(
                PropertySpec.builder(function.name, String::class)
                    .initializer("%S", function.name)
                    .addModifiers(KModifier.CONST)
                    .build()
            )
        }
        return methodObjectBuilder.build()
    }

    private fun ImmutableKmClass.buildArgObject(): TypeSpec {
        println("===> Arg")
        val argObjectBuilder = TypeSpec.objectBuilder(MockingbirdPlugin.ARG)
        for (function in this.functions) {
            for (arg in function.valueParameters) {
                argObjectBuilder.addProperty(
                    PropertySpec.builder(arg.name, String::class)
                        .initializer("%S", arg.name)
                        .addModifiers(KModifier.CONST)
                        .build()
                )
            }

        }
        return argObjectBuilder.build()
    }

    private fun isUnitFunction(function: ImmutableKmFunction): Boolean {
        val classifier = function.returnType.classifier
        return classifier is KmClassifier.Class && classifier.name == "kotlin/Unit"
    }

    private fun extractTypeString(type: ImmutableKmType): String {
        return if (type.classifier is KmClassifier.Class) {
            (type.classifier as KmClassifier.Class).name
        } else {
            throw IllegalArgumentException("I can't mock this type: ${type.classifier}")
        }
    }

    private fun extractType(type: ImmutableKmType): KClass<*> {
        val javaClass = when (val rawType = extractTypeString(type)) {
            "kotlin/String" -> "java.lang.String"
            "kotlin/Int" -> "java.lang.Integer"
            "kotlin/Long" -> "java.lang.Long"
            "kotlin/Boolean" -> "java.lang.Boolean"
            "kotlin/Double" -> "java.lang.Double"
            "kotlin/Float" -> "java.lang.Float"
            "kotlin/Short" -> "java.lang.Short"
            "kotlin/Char" -> "java.lang.Char"
            //TODo complete/ revise
            else -> rawType.replace("/", ".")
        }
        return Class.forName(javaClass).kotlin
    }

    private fun mockFunction(
        mockClassBuilder: TypeSpec.Builder,
        function: ImmutableKmFunction,
        isUnit: Boolean
    ) {
        println("===> Mocking")
        val funBuilder = FunSpec.builder(function.name)
            .addModifiers(KModifier.OVERRIDE)
        for (valueParam in function.valueParameters) {
            println(valueParam.type)
            funBuilder.addParameter(valueParam.name, extractType(valueParam.type!!))// TODO fix this
        }
        if (!isUnit) {
            funBuilder.returns(extractType(function.returnType))
        }
        funBuilder.addMockStatement(function, isUnit)
        mockClassBuilder.addFunction(
            funBuilder.build()
        )
    }

    fun FunSpec.Builder.addMockStatement(function: ImmutableKmFunction, isUnit: Boolean) {
        // TODO remove duplicates in args and method names
        val mockFunction = if (isUnit) {
            MOCK_UNIT
        } else {
            MOCK
        }
        val mockUnit = MemberName("com.careem.mockingbird.test", mockFunction)
        val v = mutableListOf<String>()
        for (i in function.valueParameters.indices) {
            v.add("Arg.%M to %S")
        }
        val args = v.joinToString(separator = ",")
        val argsValue = mutableListOf<Any>(mockUnit, MemberName("", function.name))
        for (vp in function.valueParameters) {
            argsValue.add(MemberName("", vp.name))
            argsValue.add(vp.name)
        }
        println(argsValue)
        val codeBlocks = mutableListOf("methodName = Method.%M")
        if (args.isNotEmpty()) {
            codeBlocks.add("arguments = mapOf($args)")
        }
        val statementString = """
            return %M(
                ${codeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()

        this.addStatement(statementString, *(argsValue.toTypedArray()))
    }

    companion object {
        private const val METHOD = "Method"
        private const val ARG = "Arg"
        private const val MOCK_UNIT = "mockUnit"
        private const val MOCK = "mock"
    }
}




