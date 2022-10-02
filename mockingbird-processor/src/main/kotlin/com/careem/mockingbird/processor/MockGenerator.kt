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
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class MockGenerator(
    resolver: Resolver,
    logger: KSPLogger,
    functionsMiner: FunctionsMiner
) : Generator(resolver, logger, functionsMiner) {

    override fun resolveSupertype(): String = "Mock"

    override fun decorateFunctionBody(
        classToMock: KSClassDeclaration,
        function: KSFunctionDeclaration,
        isUnit: Boolean,
        functionBuilder: FunSpec.Builder
    ) {
        val mockFunction = if (isUnit) {
            MOCK_UNIT
        } else {
            MOCK
        }
        val mockUnit = MemberName("com.careem.mockingbird.test", mockFunction)
        val v = mutableListOf<String>()
        for (i in function.parameters.indices) {
            v.add("Arg.%M to %L")
        }
        val args = v.joinToString(separator = ",")
        val argsValue = mutableListOf<Any>(mockUnit, MemberName("", function.simpleName.getShortName()))
        for (vp in function.parameters) {
            argsValue.add(MemberName("", vp.name!!.getShortName()))
            argsValue.add(vp.name!!.getShortName())
        }

        val codeBlocks = mutableListOf("methodName = Method.%M")
        if (args.isNotEmpty()) {
            codeBlocks.add("arguments = mapOf($args)")
        }
        val statementString = """
            return %M(
                ${codeBlocks.joinToString(separator = ",\n")}
            )
        """.trimIndent()

        functionBuilder.addStatement(statementString, *(argsValue.toTypedArray()))
    }

    override fun decoratePropertyGetter(
        classToMock: KSClassDeclaration,
        property: KSPropertyDeclaration,
        propertyBuilder: PropertySpec.Builder,
        typeResolver: Map<KSTypeParameter, KSTypeArgument>
    ) {
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

    override fun decoratePropertySetter(
        classToMock: KSClassDeclaration,
        property: KSPropertyDeclaration,
        propertyBuilder: PropertySpec.Builder,
        typeResolver: Map<KSTypeParameter, KSTypeArgument>
    ) {
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

    override fun decorateConstructor(classToMock: KSType, classBuilder: TypeSpec.Builder) {
        // No extra constructor parameters
    }

    companion object {
        private const val MOCK_UNIT = "mockUnit"
        private const val MOCK = "mock"
    }
}