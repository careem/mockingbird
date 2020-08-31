import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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
import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mock


interface Pippo {
    fun showRandom(): Boolean
    fun sayHi()
    fun sayHiInt(value: Int)//int
    fun sayHiWith(param: String)
//    fun sayHiWith(param: MyType)
}

//data class Taco(val seasoning: String, val soft: Boolean) {
//
//    @Mock
//    private lateinit var pippo: Pippo
//
//    fun prepare() {
//
//    }
//}

//class LoggerMock : Logger, Mock {
//    object Method {
//        const val debug = "debug"
//        const val info = "info"
//        const val error = "error"
//        const val warning = "warning"
//        const val verbose = "verbose"
//    }
//
//    object Arg {
//        const val tag = "tag"
//        const val message = "message"
//        const val throwable = "throwable"
//    }
//
//    override fun debug(tag: String, message: String) = mockUnit(
//        methodName = Method.debug,
//        arguments = mapOf(Arg.tag to tag, Arg.message to message)
//    )
//
//    override fun info(tag: String, message: String) = mockUnit(
//        methodName = Method.info,
//        arguments = mapOf(Arg.tag to tag, Arg.message to message)
//    )
//
//    override fun error(tag: String, throwable: Throwable) = mockUnit(
//        methodName = Method.error,
//        arguments = mapOf(Arg.tag to tag, Arg.throwable to throwable)
//    )
//
//    override fun error(tag: String, message: String) = mockUnit(
//        methodName = Method.error,
//        arguments = mapOf(Arg.tag to tag, Arg.message to message)
//    )
//
//    override fun error(tag: String, message: String, throwable: Throwable) = mockUnit(
//        methodName = Method.error,
//        arguments = mapOf(
//            Arg.tag to tag,
//            Arg.message to message,
//            Arg.throwable to throwable
//        )
//    )
//
//    override fun warning(tag: String, message: String) = mockUnit(
//        methodName = Method.warning,
//        arguments = mapOf(
//            Arg.tag to tag,
//            Arg.message to message
//        )
//    )
//
//    override fun warning(tag: String, message: String, throwable: Throwable) = mockUnit(
//        methodName = Method.warning,
//        arguments = mapOf(
//            Arg.tag to tag,
//            Arg.message to message,
//            Arg.throwable to throwable
//        )
//    )
//
//    override fun verbose(tag: String, message: String) = mockUnit(
//        methodName = Method.verbose,
//        arguments = mapOf(
//            Arg.tag to tag,
//            Arg.message to message
//        )
//    )
//
//}

@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockCodeGenPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // TODO delete file before build
        configureSourceSets(target)
//        extractMocks(target)

//        val clazz = Class.forName("com.careem.mockingbird.samples.PippoSample").kotlin
        val clazz = Class.forName("Pippo")
        print(clazz)
        val kmClasses = listOf(clazz.toImmutableKmClass())
        generateClasses(target, kmClasses)
    }

    private fun extractMocks(target: Project) {
        println(">>> Extraction: $target")
        extractClassMetadata()
    }

    private fun configureSourceSets(target: Project) {

        // TODO check if kmpProject before this
        target.extensions.configure(KotlinMultiplatformExtension::class.java) {
            sourceSets.getByName("commonTest") {
                kotlin.srcDir("build/generated/mockingbird")
            }
        }
    }

//    private fun generateClasses(target: Project, classNames: List<ImmutableKmClass>) {
//        generateClass(target, classNames)
////        target.subprojects.forEach {
////            generateClasses(it, classNames)
////        }
//    }

    private fun generateClasses(project: Project, classNames: List<ImmutableKmClass>) {
        for (kmClass in classNames) {
            generateMockClassFor(project, kmClass)
        }

    }

    private fun generateMockClassFor(project: Project, kmClass: ImmutableKmClass) {
        val outputDir =
            File(project.buildDir.absolutePath + File.separator + "generated" + File.separator + "mockingbird")
        outputDir.mkdirs()

        val packageName = "com.careem.mockingbird"

        // TODO fix package name
        println("Generating mocks for ${kmClass.name}")
//        val pippoSample = ClassName("com.careem.mockingbird.samples", "PippoSample")
//        pippoSample

        val greeterClass = ClassName(packageName, "${kmClass.name}Mock")
        val mockClassBuilder = TypeSpec.classBuilder("${kmClass.name}Mock")
//                    .superclass(Class.forName(kmClass.name)) // TODO fix this
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
            if (isUnitFunction(function)) {
                mockUnitFunction(mockClassBuilder, function)
            } else {
                mockFunction()
            }
        }
        // TODO support properties
        val file = FileSpec.builder(packageName, "${kmClass.name}Mock")
            .addType(mockClassBuilder.build())
//            .addFunction(
//                FunSpec.builder("main")
//                    .addParameter("args", String::class, KModifier.VARARG)
//                    .addStatement("%T(args[0]).greet()", greeterClass)
//                    .build()
//            )
            .build()

        file.writeTo(outputDir)
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
        val rawType = extractTypeString(type)
        val javaClass = when (rawType) {
            "kotlin/String" -> "java.lang.String"
            "kotlin/Int" -> "java.lang.Integer"
            "kotlin/Long" -> "java.lang.Long"
            //TODo complete
            else -> rawType.replace("/", ".")
        }
        return Class.forName(javaClass).kotlin
    }

    private fun mockUnitFunction(
        mockClassBuilder: TypeSpec.Builder,
        function: ImmutableKmFunction
    ) {

//        mockUnit(
//        methodName = Method.error,
//        arguments = mapOf(
//            Arg.tag to tag,
//            Arg.message to message,
//            Arg.throwable to throwable
//        )
//    )
        println(function.valueParameters)
        val funBuilder = FunSpec.builder(function.name)

        for (valueParam in function.valueParameters) {
//            val clazz = extractType(valueParam.type!!)
//            println(clazz)
            println(valueParam.type)
            funBuilder.addParameter(valueParam.name, extractType(valueParam.type!!))// TODO fix this
        }
        mockClassBuilder.addFunction(
            funBuilder.build()
        )
    }

    private fun mockFunction() {

    }

    private fun extractClassMetadata() {


//        val kmClass = Taco::class.toImmutableKmClass()
//
//        // Now you can access misc information about Taco from a Kotlin lens
//        println(kmClass.name)
//        kmClass.properties.forEach {
//            println(it.name)
//            println(it.jvmFlags)
//            println(it.)
//            println(it.flags)
//            println(it.hasAnnotations)
//            println("================")
//        }
//        println("Class has annotations: ${kmClass.hasAnnotations}")
//        kmClass.functions.forEach { println(it.name) }
    }
}



