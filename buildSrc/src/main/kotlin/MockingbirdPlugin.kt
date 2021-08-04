import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
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
 *
 *
 *
 * Non handled stuff:
 * 1) Abstract class? (not sure)
 *
 */


// FIXME
const val mockingBirdPath = "/Users/marcosignoretto/Documents/careem/mockingbird/"

@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockingbirdPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        try {
            // TODO delete file before build
            configureSourceSets(target)


            // TODO this requires project build already executed
            val file =
                File("${target.buildDir}/classes/kotlin/jvm/main")

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
        val externalClass = loadMockClass()

        // TODO fix package name
        println("Generating mocks for $simpleName")
        val mockClassBuilder = TypeSpec.classBuilder("${simpleName}Mock")
            .addType(kmClass.buildMethodObject())
            .addType(kmClass.buildArgObject())
            .addType(kmClass.buildPropertyObject())
            .addSuperinterface(classToMock) // TODO check if interface or generic open class
            .addSuperinterface(externalClass) // TODO fix this

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

        kmClass.properties.forEach { property ->
            this.mockProperty(mockClassBuilder, property)
        }
        // TODO support properties
        val file = FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()

        file.writeTo(outputDir)
    }

    private fun loadMockClass(): Class<*> {
        val mockDir =
            File("${mockingBirdPath}mockingbird/build/classes/kotlin/jvm/main")

        // Convert File to a URL
        val url = mockDir.toURI().toURL()          // file:/c:/myclasses/
        val urls = arrayOf(url)
        // Set kotlin class loader as parent in this way kotlin metadata will be loaded
        val cl = URLClassLoader(urls, Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = cl
        return cl.loadClass("com.careem.mockingbird.test.Mock")
    }

    private fun ImmutableKmClass.buildMethodObject(): TypeSpec {
        println("===> Methods")
        val methodObjectBuilder = TypeSpec.objectBuilder(METHOD)
        val visitedFunctionSet = mutableSetOf<String>()
        for (function in this.functions) {
            val functionName = function.name
            if (!visitedFunctionSet.contains(functionName)) {
                visitedFunctionSet.add(functionName)
                methodObjectBuilder.addProperty(
                    PropertySpec.builder(functionName, String::class)
                        .initializer("%S", functionName)
                        .addModifiers(KModifier.CONST)
                        .build()
                )
            }
        }
        return methodObjectBuilder.build()
    }

    private fun ImmutableKmClass.buildArgObject(): TypeSpec {
        println("===> Arg")
        val argObjectBuilder = TypeSpec.objectBuilder(ARG)
        val visitedPropertySet = mutableSetOf<String>()
        for (function in this.functions) {
            for (arg in function.valueParameters) {
                val argName = arg.name
                println("ARG $argName")
                if (!visitedPropertySet.contains(argName)) {
                    visitedPropertySet.add(argName)
                    argObjectBuilder.addProperty(
                        PropertySpec.builder(argName, String::class)
                            .initializer("%S", argName)
                            .addModifiers(KModifier.CONST)
                            .build()
                    )
                }
            }

        }
        return argObjectBuilder.build()
    }

    private fun ImmutableKmClass.buildPropertyObject(): TypeSpec {
        println("===> Prop")
        val propertyObjectBuilder = TypeSpec.objectBuilder(PROPERTY)
        var haveMutableProps = false
        val visitedPropertySet = mutableSetOf<String>()
        this.properties.forEach { property ->
            println("===> Prop $property")
            property.getterSignature?.let {
                handleProperty(it.name, visitedPropertySet, propertyObjectBuilder)
            }

            property.setterSignature?.let {
                haveMutableProps = true
                handleProperty(it.name, visitedPropertySet, propertyObjectBuilder)
            }
        }

        if (haveMutableProps) {
            val setterValueProperty = buildProperty(PROPERTY_SETTER_VALUE)
            propertyObjectBuilder.addProperty(setterValueProperty)
        }
        return propertyObjectBuilder.build()
    }

    private fun handleProperty(
        name: String,
        visited: MutableSet<String>,
        builder: TypeSpec.Builder
    ) {
        if (!visited.contains(name)) {
            visited.add(name)
            val nameProperty = buildProperty(name)
            builder.addProperty(nameProperty)
        }
    }

    private fun buildProperty(name: String) =
        PropertySpec.builder(name, String::class)
            .initializer("%S", name)
            .addModifiers(KModifier.CONST)
            .build()

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

    private fun mockProperty(
        mockClassBuilder: TypeSpec.Builder,
        property: ImmutableKmProperty
    ) {
        println("===> Mocking Property ${property.getterSignature?.name} and ${property.setterSignature?.name} and ${property.setterSignature}")
        val type = extractType(property.returnType)

        val propertyBuilder = PropertySpec
            .builder(property.name, type, KModifier.OVERRIDE)

        if (property.getterSignature != null) {
            val getterBuilder = FunSpec.getterBuilder()
            val mockFunction = MemberName("com.careem.mockingbird.test", MOCK)
            val getterArgsValue = mutableListOf(
                mockFunction,
                MemberName(
                    "",
                    property.getterSignature?.name
                        ?: throw java.lang.IllegalArgumentException("I can't mock this property")
                )
            )
            val getterCodeBlocks = mutableListOf("methodName = $PROPERTY.%M")
            val getterStatementString = """
            return %M(
                ${getterCodeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()
            getterBuilder.addStatement(getterStatementString, *(getterArgsValue.toTypedArray()))
            propertyBuilder.getter(getterBuilder.build())
        }

        if (property.setterSignature != null) {
            val setterBuilder = FunSpec.setterBuilder()
            val mockUnitFunction = MemberName("com.careem.mockingbird.test", MOCK_UNIT)
            val setterArgsValue = mutableListOf(
                mockUnitFunction,
                MemberName(
                    "",
                    property.setterSignature?.name
                        ?: throw java.lang.IllegalArgumentException("I can't mock this property")
                ),
                MemberName("", PROPERTY_SETTER_VALUE),
                PROPERTY_SETTER_VALUE
            )

            val v = mutableListOf<String>().apply {
                add("Property.%M to %L")
            }
            val args = v.joinToString(separator = ",")
            val setterCodeBlocks = mutableListOf("methodName = $PROPERTY.%M")
            setterCodeBlocks.add("arguments = mapOf($args)")
            val setterStatementString = """
            return %M(
                ${setterCodeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()
            setterBuilder
                .addParameter("value", type)
                .addStatement(setterStatementString, *(setterArgsValue.toTypedArray()))
            propertyBuilder
                .mutable()
                .setter(setterBuilder.build())
        }

        mockClassBuilder.addProperty(propertyBuilder.build())
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

    private fun FunSpec.Builder.addMockStatement(function: ImmutableKmFunction, isUnit: Boolean) {
        // TODO remove duplicates in args and method names
        val mockFunction = if (isUnit) {
            MOCK_UNIT
        } else {
            MOCK
        }
        val mockUnit = MemberName("com.careem.mockingbird.test", mockFunction)
        val v = mutableListOf<String>()
        for (i in function.valueParameters.indices) {
            v.add("Arg.%M to %L")
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
        private const val PROPERTY = "Property"
        private const val MOCK_UNIT = "mockUnit"
        private const val MOCK = "mock"
        private const val PROPERTY_SETTER_VALUE = "value"
    }
}




