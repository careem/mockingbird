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
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.TypeSpec

class MockGenerator(
    resolver: Resolver,
    logger: KSPLogger,
    functionsMiner: FunctionsMiner
) : CodeBlockFactory {

    private val generator : Generator = Generator(resolver, logger, functionsMiner, this)

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


    override fun decorateConstructor(classToMock: KSClassDeclaration, classBuilder: TypeSpec.Builder) {
        // No extra constructor parameters
    }

    fun createClass(ksTypeRef: KSTypeReference): FileSpec = generator.createClass(ksTypeRef)

    companion object {
        private const val MOCK_UNIT = "mockUnit"
        private const val MOCK = "mock"
    }
}