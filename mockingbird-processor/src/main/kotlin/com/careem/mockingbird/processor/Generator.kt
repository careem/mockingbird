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

package com.careem.mockingbird.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

class Generator constructor(
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val functionsMiner: FunctionsMiner,
    private val codeBlockFactory: CodeBlockFactory
) {

    fun createClass(ksTypeRef: KSTypeReference): FileSpec {

        val classToMock = ksTypeRef.resolve()

        val classGenerics = classToMock.arguments.map { "_${it.type.toString()}" }.joinToString("")

        val className = classToMock.toClassName()
        val simpleName = className.simpleName

        val ksClassDeclaration = classToMock.declaration as KSClassDeclaration

        val typeResolver: Map<KSTypeParameter, KSTypeArgument> =
            ksClassDeclaration.typeParameters.zip(classToMock.arguments).toMap()

        val packageName = className.packageName
        val externalClass =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString("com.careem.mockingbird.test.${codeBlockFactory.resolveSupertype()}"))

        logger.info("Generating mocks for $simpleName")

        val (functionsToMock, propertiesToMock) = functionsMiner.extractFunctionsAndProperties(ksClassDeclaration)

        val mockClassBuilder = TypeSpec.classBuilder("${simpleName}${classGenerics}${codeBlockFactory.resolveSupertype()}")
            .addType(functionsToMock.buildMethodObject())
            .addType(functionsToMock.buildArgObject())
            .addType(propertiesToMock.buildPropertyObject())
            .addSuperinterface(classToMock.toTypeName())
            .addSuperinterface(externalClass!!.toClassName())
        codeBlockFactory.decorateConstructor(ksClassDeclaration, mockClassBuilder)

        ksClassDeclaration.modifiers.filter {
            it == Modifier.PUBLIC || it == Modifier.INTERNAL || it == Modifier.PROTECTED || it == Modifier.PRIVATE
        }.forEach { modifier ->
            modifier.toKModifier()?.let { mockClassBuilder.addModifiers(it) }
        }

        functionsToMock.forEach { function ->
            mockFunction(ksClassDeclaration, typeResolver, mockClassBuilder, function, isUnitFunction(function))
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
            mockProperty(typeResolver, mockClassBuilder, property)
        }

        val mockedClass = mockClassBuilder.build()

        return FileSpec.builder(packageName, "${simpleName}${codeBlockFactory.resolveSupertype()}")
            .addType(mockedClass)
            .build()
    }

    private fun List<KSFunctionDeclaration>.buildMethodObject(): TypeSpec {
        logger.info("Generating methods")
        val methodObjectBuilder = TypeSpec.objectBuilder(METHOD)
        val visitedFunctionSet = mutableSetOf<String>()
        for (function in this) {
            val functionName = function.simpleName.getShortName()
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

    private fun List<KSFunctionDeclaration>.buildArgObject(): TypeSpec {
        logger.info("Generating arguments")
        val argObjectBuilder = TypeSpec.objectBuilder(ARG)
        val visitedPropertySet = mutableSetOf<String>()
        for (function in this) {
            for (arg in function.parameters) {
                val argName = arg.name!!.getShortName()
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

    private fun List<KSPropertyDeclaration>.buildPropertyObject(): TypeSpec {
        logger.info("Generating properties")
        val propertyObjectBuilder = TypeSpec.objectBuilder(PROPERTY)
        var haveMutableProps = false
        val visitedPropertySet = mutableSetOf<String>()
        this.forEach { property ->
            logger.info("Property: $property")
            val rawName = property.simpleName.getShortName()
            property.getter?.let {
                handleProperty(adjustPropertyName(true, rawName), visitedPropertySet, propertyObjectBuilder)
            }

            property.setter?.let {
                haveMutableProps = true
                handleProperty(adjustPropertyName(false, rawName), visitedPropertySet, propertyObjectBuilder)
            }
        }

        if (haveMutableProps) {
            val setterValueProperty = buildProperty(PROPERTY_SETTER_VALUE)
            propertyObjectBuilder.addProperty(setterValueProperty)
        }
        return propertyObjectBuilder.build()
    }

    private fun adjustPropertyName(isGetter: Boolean, rawName: String): String {
        val prefix = if (isGetter) {
            if (rawName.startsWith("is", ignoreCase = true)) ""
            else "get"
        } else {
            "set"
        }
        val newName = if (prefix.isNotEmpty()) rawName.replaceFirstChar(Char::titlecase) else rawName
        return "$prefix$newName"
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

    private fun isUnitFunction(function: KSFunctionDeclaration): Boolean {
        val classifier = function.returnType
        val ksType = classifier!!.resolve()
        return ksType.fullyQualifiedName() == "kotlin.Unit"
    }

    private fun mockProperty(
        typeResolver: Map<KSTypeParameter, KSTypeArgument>,
        mockClassBuilder: TypeSpec.Builder,
        property: KSPropertyDeclaration
    ) {
        logger.info("===> Mocking Property ${property.getter} and ${property.setter}")
        val propertyBuilder = PropertySpec
            .builder(
                property.simpleName.getShortName(),
                property.type.toTypeNameResolved(typeResolver),
                KModifier.OVERRIDE
            )
        if (property.getter != null) {
            val getterBuilder = FunSpec.getterBuilder()
            val mockFunction = MemberName("com.careem.mockingbird.test", MOCK)
            val getterArgsValue = mutableListOf(
                mockFunction,
                MemberName(
                    "",
                    adjustPropertyName(true, property.simpleName.getShortName())
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

        if (property.setter != null) {
            val setterBuilder = FunSpec.setterBuilder()
            val mockUnitFunction = MemberName("com.careem.mockingbird.test", MOCK_UNIT)
            val setterArgsValue = mutableListOf(
                mockUnitFunction,
                MemberName(
                    "",
                    adjustPropertyName(false, property.simpleName.getShortName())
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
                .addParameter("value", property.type.toTypeNameResolved(typeResolver))
                .addStatement(setterStatementString, *(setterArgsValue.toTypedArray()))
            propertyBuilder
                .mutable()
                .setter(setterBuilder.build())
        }

        mockClassBuilder.addProperty(propertyBuilder.build())
    }

    private fun buildFunctionModifiers(
        function: KSFunctionDeclaration
    ): List<KModifier> {
        return buildList {
            getFunctionVisibility(function.modifiers)?.let { add(it) }
            add(KModifier.OVERRIDE)
            if (function.modifiers.contains(Modifier.SUSPEND)) {
                add(KModifier.SUSPEND)
            }
        }
    }

    private fun getFunctionVisibility(modifiers: Set<Modifier>) =
        if (modifiers.contains(Modifier.INTERNAL)) {
            KModifier.INTERNAL
        } else if (modifiers.contains(Modifier.PRIVATE)) {
            KModifier.PRIVATE
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            KModifier.PROTECTED
        } else if (modifiers.contains(Modifier.PUBLIC)) {
            KModifier.PUBLIC
        } else null

    private fun mockFunction(
        classToMock: KSClassDeclaration,
        typeResolver: Map<KSTypeParameter, KSTypeArgument>,
        mockClassBuilder: TypeSpec.Builder,
        function: KSFunctionDeclaration,
        isUnit: Boolean
    ) {
        logger.info("Mocking function")
        val funBuilder = FunSpec.builder(function.simpleName.getShortName())
            .addModifiers(buildFunctionModifiers(function))
        for (valueParam in function.parameters) {
            logger.info(valueParam.type.toString())
            funBuilder.addParameter(
                valueParam.name!!.getShortName(),
                valueParam.type.toTypeNameResolved(typeResolver)
            )
        }
        if (!isUnit) {
            funBuilder.returns(function.returnType!!.toTypeNameResolved(typeResolver))
        }
        funBuilder.addMockStatement(classToMock, function, isUnit)
        mockClassBuilder.addFunction(
            funBuilder.build()
        )
    }

    fun KSTypeReference.toTypeNameResolved(typeResolver: Map<KSTypeParameter, KSTypeArgument>): TypeName {
        return if (typeResolver.containsKey(this.resolve().declaration)) {
            typeResolver.get(this.resolve().declaration)!!.toTypeName()
        } else {
            this.toTypeName()
        }
    }

    private fun FunSpec.Builder.addMockStatement(classToMock: KSClassDeclaration, function: KSFunctionDeclaration, isUnit: Boolean) {
        codeBlockFactory.decorateFunctionBody(classToMock, function, isUnit, this)
    }

    companion object {
        private const val METHOD = "Method"
        private const val ARG = "Arg"
        private const val PROPERTY = "Property"
        private const val MOCK = "mock" // TODO remove
        private const val MOCK_UNIT = "mockUnit" // TODO remove

        private const val PROPERTY_SETTER_VALUE = "value"
    }

}