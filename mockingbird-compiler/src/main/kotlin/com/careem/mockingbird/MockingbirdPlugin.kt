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
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File
import kotlin.reflect.KClass

private const val EXTENSION_NAME = "mockingBird"


@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
abstract class MockingbirdPlugin : Plugin<Project> {

    private lateinit var classLoader: ClassLoaderWrapper
    private lateinit var functionsMiner: FunctionsMiner
    private lateinit var projectExplorer: ProjectExplorer
    private val logger: Logger = Logging.getLogger(this::class.java)

    private fun setupDependencies(target: Project) {
        classLoader = ClassLoaderWrapper(target)
        functionsMiner = FunctionsMiner(classLoader)
    }

    override fun apply(target: Project) {
        projectExplorer = ProjectExplorer()
        try {
            configureSourceSets(target)

            target.extensions.add<MockingbirdPluginExtension>(
                EXTENSION_NAME, MockingbirdPluginExtensionImpl(target.objects)
            )
//
//            val explore = target.task("explore") {
//                dependsOn(target.tasks.getByName("assemble"))
//                doLast {
//                    // FIXME not workign because: Cannot change dependencies of dependency configuration ':sample:commonTestImplementation' after it has been included in dependency resolution.
//                    projectExplorer.exploreProject(target.rootProject)
//                    val dependencySet = projectExplorer.explore(target)
//                    target.extensions.getByType(KotlinMultiplatformExtension::class.java).run {
//                        // FIXME here commontTest is not enought I need to add also to other KMP target if library have one
//                        sourceSets.getByName("commonTest") {
//                            dependencies {
//                                dependencySet.forEach { implementation(it) }
//                                //implementation("org.jetbrains.kotlinx:atomicfu:0.16.2")
//                            }
//                        }
//                    }
//                }
//            }

            target.task("generateMocks") {
                dependsOn(target.tasks.getByName("assemble"))
                doLast {
                    generateMocks(target)
                }
            }

            // Add test dependencies for classes that need to be mocked
            target.afterEvaluate {
                projectExplorer.exploreProject(target.rootProject)
                val dependencySet = projectExplorer.explore(target)
                target.extensions.getByType(KotlinMultiplatformExtension::class.java).run {
                    // FIXME here commontTest is not enought I need to add also to other KMP target if library have one
                    sourceSets.getByName("commonTest") {
                        dependencies {
                            dependencySet.forEach { implementation(it) }
                            //implementation("org.jetbrains.kotlinx:atomicfu:0.16.2")
                        }
                    }
                }
//                target.extensions.getByType(KotlinMultiplatformExtension::class.java).run {
//                    sourceSets.getByName("commonTest") {
//                        dependencies {
//                            implementation("org.jetbrains.kotlinx:atomicfu:0.16.2")
//                        }
//                    }
//                }
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
        setupDependencies(target)

        projectExplorer.explore(target)

        val pluginExtensions = target.extensions[EXTENSION_NAME] as MockingbirdPluginExtensionImpl
        logger.info("Mocking: ${pluginExtensions.generateMocksFor}")

        for (className in pluginExtensions.generateMocksFor) {
            val externalClass = classLoader.loadClass(className)
            val kmClasses = listOf(externalClass.toImmutableKmClass())
            generateClasses(target, kmClasses)
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
        val classToMock = classLoader.loadClass(kmClass)
        val simpleName = kmClass.name.substringAfterLast("/")
        val outputDir =
            File(project.buildDir.absolutePath + File.separator + "generated" + File.separator + "mockingbird")
        outputDir.mkdirs()

        val packageName = classToMock.qualifiedName!!.substringBeforeLast(".")
        val externalClass = loadMockClass()

        logger.debug("Generating mocks for $simpleName")

        val (functionsToMock, propertiesToMock) = functionsMiner.extractFunctionsAndProperties(kmClass)

        val mockClassBuilder = TypeSpec.classBuilder("${simpleName}Mock")
            .addType(functionsToMock.buildMethodObject())
            .addType(functionsToMock.buildArgObject())
            .addType(propertiesToMock.buildPropertyObject())
            .addSuperinterface(classToMock) // TODO check if interface or generic open class
            .addSuperinterface(externalClass) // TODO fix this


        functionsToMock.forEach { function ->
            mockFunction(mockClassBuilder, function, isUnitFunction(function))
        }

        propertiesToMock.forEach { property ->
            mockProperty(mockClassBuilder, property)
        }

        val file = FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()

        file.writeTo(outputDir)
    }

    private fun loadMockClass(): KClass<*> {
        return classLoader.loadClass("com.careem.mockingbird.test.Mock")
    }

    private fun List<ImmutableKmFunction>.buildMethodObject(): TypeSpec {
        logger.info("Generating methods")
        val methodObjectBuilder = TypeSpec.objectBuilder(METHOD)
        val visitedFunctionSet = mutableSetOf<String>()
        for (function in this) {
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

    private fun List<ImmutableKmFunction>.buildArgObject(): TypeSpec {
        logger.info("Generating arguments")
        val argObjectBuilder = TypeSpec.objectBuilder(ARG)
        val visitedPropertySet = mutableSetOf<String>()
        for (function in this) {
            for (arg in function.valueParameters) {
                val argName = arg.name
                logger.info("Argument: $argName")
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

    private fun List<ImmutableKmProperty>.buildPropertyObject(): TypeSpec {
        logger.info("Generating properties")
        val propertyObjectBuilder = TypeSpec.objectBuilder(PROPERTY)
        var haveMutableProps = false
        val visitedPropertySet = mutableSetOf<String>()
        this.forEach { property ->
            logger.info("Property: $property")
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

    private fun mockProperty(
        mockClassBuilder: TypeSpec.Builder,
        property: ImmutableKmProperty
    ) {
        logger.debug("===> Mocking Property ${property.getterSignature?.name} and ${property.setterSignature?.name} and ${property.setterSignature}")
        val type = classLoader.loadClass(property.returnType)

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
        logger.info("Mocking function")
        val funBuilder = FunSpec.builder(function.name)
            .addModifiers(KModifier.OVERRIDE)
        for (valueParam in function.valueParameters) {
            logger.info(valueParam.type.toString())
            funBuilder.addParameter(valueParam.name, classLoader.loadClass(valueParam.type!!))// TODO fix this
        }
        if (!isUnit) {
            funBuilder.returns(classLoader.loadClass(function.returnType))
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
        logger.debug(argsValue.toString())
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




