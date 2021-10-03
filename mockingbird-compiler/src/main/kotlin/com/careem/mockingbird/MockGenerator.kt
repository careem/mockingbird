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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import kotlinx.metadata.KmClassifier
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import kotlin.reflect.KClass

@OptIn(KotlinPoetMetadataPreview::class)
class MockGenerator constructor(
    private val classLoader: ClassLoaderWrapper,
    private val functionsMiner: FunctionsMiner
) {

    private val logger: Logger = Logging.getLogger(this::class.java)


    fun createClass(kmClass: ImmutableKmClass): FileSpec {
        val classToMock = classLoader.loadClass(kmClass)
        val simpleName = kmClass.name.substringAfterLast("/")

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

        return FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()
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

    private fun loadMockClass(): KClass<*> {
        return classLoader.loadClass("com.careem.mockingbird.test.Mock")
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
        val propertyBuilder = PropertySpec
            .builder(
                property.name,
                property.returnType.buildType(),
                KModifier.OVERRIDE
            )

        if (property.getterSignature != null) {
            val getterBuilder = FunSpec.getterBuilder()
            val mockFunction = MemberName("com.careem.mockingbird.test", MOCK)
            val getterArgsValue = mutableListOf(
                mockFunction,
                MemberName(
                    "",
                    property.getterSignature?.name
                        ?: throw IllegalArgumentException("I can't mock this property")
                )
            )
            val getterCodeBlocks = mutableListOf("methodName = ${PROPERTY}.%M")
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
                        ?: throw IllegalArgumentException("I can't mock this property")
                ),
                MemberName("", PROPERTY_SETTER_VALUE),
                PROPERTY_SETTER_VALUE
            )

            val v = mutableListOf<String>().apply {
                add("Property.%M to %L")
            }
            val args = v.joinToString(separator = ",")
            val setterCodeBlocks = mutableListOf("methodName = ${PROPERTY}.%M")
            setterCodeBlocks.add("arguments = mapOf($args)")
            val setterStatementString = """
            return %M(
                ${setterCodeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()
            setterBuilder
                .addParameter("value", property.returnType.buildType())
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
            val kmType = valueParam.type!!
            funBuilder.addParameter(valueParam.name, kmType.buildType())
        }
        if (!isUnit) {
            funBuilder.returns(function.returnType.buildType())
        }
        funBuilder.addMockStatement(function, isUnit)
        mockClassBuilder.addFunction(
            funBuilder.build()
        )
    }

    private fun ImmutableKmType.buildType(): TypeName {
        val arguments = this.arguments
            .map { classLoader.loadClass(it.type!!).asTypeName() }// TODO support suspend fun ?
        val result = classLoader.loadClass(this)
            .asTypeName()
            .let {
                if (arguments.isNotEmpty()) {
                    it.parameterizedBy(arguments)
                } else {
                    it
                }
            }.copy(
                nullable = this.isNullable
            )
        println(result)
        return result
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