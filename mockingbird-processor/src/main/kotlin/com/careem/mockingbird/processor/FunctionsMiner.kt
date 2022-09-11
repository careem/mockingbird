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

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

class FunctionsMiner {

    /**
     * Extract all functions and properties, this will extract also functions that are defined into the supertypes
     * these functions are all the functions that the mock class should provide
     */
    fun extractFunctionsAndProperties(kmClass: KSClassDeclaration): Pair<List<KSFunctionDeclaration>, List<KSPropertyDeclaration>> {
        val functions: MutableList<KSFunctionDeclaration> = mutableListOf()
        val properties: MutableList<KSPropertyDeclaration> = mutableListOf()

        val kmSuperTypes = kmClass.getAllSuperTypes()
            .filter { it.fullyQualifiedName() != "kotlin.Any" }
            .map { it.declaration }
            .filter { it is KSClassDeclaration }
            .map { it as KSClassDeclaration }

        (listOf(kmClass) + kmSuperTypes).forEach { rawExtractFunctionsAndProperties(it, functions, properties) }
        return functions to properties
    }

    private fun rawExtractFunctionsAndProperties(
        kmClass: KSClassDeclaration,
        functions: MutableList<KSFunctionDeclaration>,
        properties: MutableList<KSPropertyDeclaration>
    ) {
        // get functions and properties for current class
        functions.addAll(kmClass.getDeclaredFunctions().filterNot { it.modifiers.contains(Modifier.OVERRIDE) })
        properties.addAll(kmClass.getDeclaredProperties().filterNot { it.modifiers.contains(Modifier.OVERRIDE) })
    }
}