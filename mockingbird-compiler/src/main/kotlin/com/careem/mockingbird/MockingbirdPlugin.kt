/*
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.careem.mockingbird

import com.squareup.kotlinpoet.DelicateKotlinPoetApi
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
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass

private const val EXTENSION_NAME = "mockingBird"


@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockingbirdPlugin : Plugin<Project> {

    private lateinit var classLoader : ClassLoader

    override fun apply(target: Project) {
        try {
            configureSourceSets(target)

            target.extensions.add<MockingbirdPluginExtension>(
                EXTENSION_NAME, MockingbirdPluginExtensionImpl(target.objects)
            )

            target.task("generateMocks") {
                dependsOn(target.tasks.getByName("assemble"))
                doLast {
                    generateMocks(target)
                }
            }

            target.tasks.getByName("allTests") {
                dependsOn(target.tasks.getByName("generateMocks"))
            }
        } catch (e: Exception) {
            // Useful to debug
            e.printStackTrace()
            throw e
        }
    }

    private fun generateMocks(target: Project) {
        val pluginExtensions = target.extensions[EXTENSION_NAME] as MockingbirdPluginExtensionImpl
        println("MOCKS: ${pluginExtensions.generateMocksFor}")

        setupClassLoader(target)

        for (className in pluginExtensions.generateMocksFor) {
            val externalClass = classLoader.loadClass(className)
            val kmClasses = listOf(externalClass.toImmutableKmClass())
            generateClasses(target, kmClasses)
        }

    }

    private fun setupClassLoader(target: Project){
        // Add all subproject to classpath TODO this can be optimized, no need to add all of them
        val urlList = mutableListOf<URL>()
        traverseDependencyTree(target.rootProject, urlList)

        // Set kotlin class loader as parent in this way kotlin metadata will be loaded
        val cl = URLClassLoader(urlList.toTypedArray(), Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = cl
        classLoader = cl
    }

    private fun traverseDependencyTree(target: Project, mutableList: MutableList<URL>){
        target.subprojects.forEach {  // TODO improve performance skipping to traverse duplicated dependencies ( eg A -> B -> C and D -> B -> C do not need to explore B-> C again since I did earlier )
            val file = File("${it.buildDir}/classes/kotlin/jvm/main")
            // Convert File to a URL
            val url = file.toURI().toURL()
            mutableList.add(url)
            traverseDependencyTree(it, mutableList)
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


    @OptIn(DelicateKotlinPoetApi::class)
    private fun generateMockClassFor(project: Project, kmClass: ImmutableKmClass) {
        val classToMock = classLoader.loadClass(
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

        for (function in kmClass.functions) {
            this.mockFunction(mockClassBuilder, function, isUnitFunction(function))
        }

        kmClass.properties.forEach { property ->
            this.mockProperty(mockClassBuilder, property)
        }

        val file = FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()

        file.writeTo(outputDir)
    }

    private fun loadMockClass(): Class<*> {
        return classLoader.loadClass("com.careem.mockingbird.test.Mock")
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
            else -> {
                rawType.replace("/", ".")
            }
        }

        return classLoader.loadClass(javaClass).kotlin
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




