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
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isInternal
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.isPrivate
import com.squareup.kotlinpoet.metadata.isProtected
import com.squareup.kotlinpoet.metadata.isPublic
import com.squareup.kotlinpoet.metadata.isSuspend
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import kotlinx.metadata.jvm.getterSignature
import kotlinx.metadata.jvm.setterSignature
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility

@OptIn(KotlinPoetMetadataPreview::class)
class MockGenerator constructor(
    private val classLoader: ClassLoaderWrapper,
    private val functionsMiner: FunctionsMiner
) {

    private val logger: Logger = Logging.getLogger(this::class.java)


    fun createClass(kmClass: KmClass): FileSpec {
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

        getClassVisibility(classToMock.visibility)?.let { mockClassBuilder.addModifiers(it) }

        functionsToMock.forEach { function ->
            mockFunction(mockClassBuilder, function, isUnitFunction(function))
        }

        val uuid = PropertySpec.builder("uuid", String::class, KModifier.OVERRIDE)
            .addModifiers(KModifier.PUBLIC)
            .delegate(
                buildCodeBlock {
                    val uuid = MemberName("com.careem.mockingbird.test", "uuid")
                    add("%M()", uuid)
                })
            .build()

        mockClassBuilder.addProperty(uuid)

        propertiesToMock.forEach { property ->
            mockProperty(mockClassBuilder, property)
        }

        return FileSpec.builder(packageName, "${simpleName}Mock")
            .addType(mockClassBuilder.build())
            .build()
    }

    private fun getClassVisibility(visibility: KVisibility?) =
        when (visibility) {
            KVisibility.PUBLIC -> KModifier.PUBLIC
            KVisibility.PROTECTED -> KModifier.PROTECTED
            KVisibility.INTERNAL -> KModifier.INTERNAL
            KVisibility.PRIVATE -> KModifier.INTERNAL
            null -> null
        }

    private fun List<KmFunction>.buildMethodObject(): TypeSpec {
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

    private fun List<KmFunction>.buildArgObject(): TypeSpec {
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

    private fun List<KmProperty>.buildPropertyObject(): TypeSpec {
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

    private fun isUnitFunction(function: KmFunction): Boolean {
        val classifier = function.returnType.classifier
        return classifier is KmClassifier.Class && classifier.name == "kotlin/Unit"
    }

    private fun mockProperty(
        mockClassBuilder: TypeSpec.Builder,
        property: KmProperty
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

    private fun buildFunctionModifiers(
        function: KmFunction
    ): List<KModifier> {
        return buildList {
            getFunctionVisibility(function.flags)?.let { add(it) }
            add(KModifier.OVERRIDE)
            if (function.isSuspend) {
                add(KModifier.SUSPEND)
            }
        }
    }

    private fun getFunctionVisibility(flags: Flags) =
        if (flags.isInternal) {
            KModifier.INTERNAL
        } else if (flags.isPrivate) {
            KModifier.PRIVATE
        } else if (flags.isProtected) {
            KModifier.PROTECTED
        } else if (flags.isPublic) {
            KModifier.PUBLIC
        } else null

    private fun mockFunction(
        mockClassBuilder: TypeSpec.Builder,
        function: KmFunction,
        isUnit: Boolean
    ) {
        logger.info("Mocking function")
        val funBuilder = FunSpec.builder(function.name)
            .addModifiers(buildFunctionModifiers(function))
        for (valueParam in function.valueParameters) {
            logger.info(valueParam.type.toString())
            val kmType = valueParam.type
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

    private fun KmType.buildType(): TypeName {
        val subTypes = this.arguments.map { it.type!! }
        return classLoader.loadClass(this)
            .asTypeName()
            .let {
                if (subTypes.isEmpty()) {
                    it
                } else {
                    val typeNames = subTypes.map { subType ->
                        subType.buildType()
                    }
                    it.parameterizedBy(typeNames)
                }
            }.copy(
                nullable = this.isNullable
            )
    }


    private fun FunSpec.Builder.addMockStatement(function: KmFunction, isUnit: Boolean) {
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