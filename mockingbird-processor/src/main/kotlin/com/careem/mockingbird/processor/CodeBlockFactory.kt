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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

interface CodeBlockFactory {
    fun resolveSupertype(): String
    fun decorateConstructor(classToMock: KSClassDeclaration, classBuilder: TypeSpec.Builder)
    fun decorateFunctionBody(classToMock: KSClassDeclaration, function: KSFunctionDeclaration, isUnit: Boolean, functionBuilder: FunSpec.Builder)
}