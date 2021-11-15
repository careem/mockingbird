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

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier

@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
class FunctionsMiner(
    private val classLoaderWrapper: ClassLoaderWrapper
) {

    /**
     * Extract all functions and properties, this will extract also functions that are defined into the supertypes
     * these functions are all the functions that the mock class should provide
     */
    fun extractFunctionsAndProperties(kmClass: ImmutableKmClass): Pair<List<ImmutableKmFunction>, List<ImmutableKmProperty>> {
        val functions: MutableList<ImmutableKmFunction> = mutableListOf()
        val properties: MutableList<ImmutableKmProperty> = mutableListOf()
        rawExtractFunctionsAndProperties(kmClass, functions, properties)
        return functions.distinctBy { it.signature } to properties
            .filter { it.getterSignature != null || it.setterSignature != null }
            .distinctBy { it.getterSignature ?: it.setterSignature }
    }

    private fun rawExtractFunctionsAndProperties(
        kmClass: ImmutableKmClass,
        functions: MutableList<ImmutableKmFunction>,
        properties: MutableList<ImmutableKmProperty>
    ) { // TODO optimize with tailrec
        val kmSuperTypes = kmClass.supertypes
            .map { it.classifier }
            .filterIsInstance<KmClassifier.Class>()
            .filter { it.name != "kotlin/Any" }
            .map { classLoaderWrapper.loadClassFromDirectory(it.name).toImmutableKmClass() }

        // get functions and properties for current class
        functions.addAll(kmClass.functions)
        properties.addAll(kmClass.properties)

        // check each super type and extract functions and properties
        kmSuperTypes.forEach { rawExtractFunctionsAndProperties(it, functions, properties) }
    }
}