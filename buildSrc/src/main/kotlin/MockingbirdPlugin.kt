import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File
import java.net.URLClassLoader
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
abstract class MockingbirdPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // TODO delete file before build
        configureSourceSets(target)
//        extractMocks(target)


//        val header = KotlinClassHeader(
//
//        /* pass Metadata.k, Metadata.d1, Metadata.d2, etc as arguments ... */
//        )
//        val metadata = KotlinClassMetadata.read(header)


        // TODO this requires project build already executed
        val context = Thread.currentThread().contextClassLoader
        println("Context calss loader:${context}")
        val file = File("/Users/marcosignoretto/Documents/careem/mockingbird/samples/build/classes/kotlin/jvm/main")

        // Convert File to a URL
        val url = file.toURI().toURL()          // file:/c:/myclasses/
        val urls = arrayOf(url)
        val cl = URLClassLoader(urls, Thread.currentThread().contextClassLoader) // FIXME tis class loaded is not loading kotlin Metadata
        Thread.currentThread().contextClassLoader = cl
//        val externalClass = cl.loadClass("com.careem.mockingbird.samples.MyDependency")
        val externalClass = cl.loadClass("com.careem.mockingbird.samples.PippoSample")
        //externalClass.toImmutableKmClass() // FIXME this crash because Metadata not found but it is there
        println(externalClass.getAnnotation(Metadata::class.java)) //FIXME this prints null

        println("annotations")

        // FIXME I can't get metadata
        for (ann in externalClass.annotations) {
            println(ann)
        }
        println("++++")


        println("tricky annotation")
        val metaClass = externalClass.asClassName()
//        val annotation: Annotation? = metaClass.getAnnotation(Metadata::class.java)
//        if(annotation == null){
//            return getMetadata(element.enclosingElement!!)
//        }
        println(metaClass.packageName)
        println(metaClass.annotations)

        println(externalClass)

//        for (method in externalClass.declaredMethods) {
//            println(method.returnType)
//            println(method.name)
//            println("Parameters:")
//            println("(")
//            for (param in method.parameters) {
//                println(param.name)
//                println(param.type)
//            }
//            println(")")
//        }


//        val clazz = Class.forName("com.careem.mockingbird.samples.PippoSample").kotlin
        val clazz = Class.forName("Pippo")
        print(clazz)
        val kmClasses = listOf(clazz.toImmutableKmClass())
        generateClasses(target, kmClasses)
    }

    fun getMetadata(clazz: Class<*>): KotlinClassMetadata {
        val annotation = clazz.getAnnotation(Metadata::class.java)
        if (annotation == null) {
            return getMetadata(clazz.enclosingClass)
        }

        return annotation.let {
            KotlinClassHeader(
                it.kind, it.metadataVersion, it.bytecodeVersion,
                it.data1, it.data2, it.extraString, it.packageName, it.extraInt
            )
        }.let {
            KotlinClassMetadata.read(it)!!
        }
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
            .addType(kmClass.buildMethodObject())
            .addType(kmClass.buildArgObject())
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
            this.mockFunction(mockClassBuilder, function, isUnitFunction(function))
        }
        // TODO support properties
        val file = FileSpec.builder(packageName, "${kmClass.name}Mock")
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
            MockingbirdPlugin.MOCK_UNIT
        } else {
            MockingbirdPlugin.MOCK
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

    companion object {
        private const val METHOD = "Method"
        private const val ARG = "Arg"
        private const val MOCK_UNIT = "mockUnit"
        private const val MOCK = "mock"
    }
}




