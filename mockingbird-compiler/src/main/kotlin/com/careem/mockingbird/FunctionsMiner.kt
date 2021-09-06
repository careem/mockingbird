package com.careem.mockingbird

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmFunction
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutableKmClass
import kotlinx.metadata.KmClassifier
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@Suppress("UnstableApiUsage")
@KotlinPoetMetadataPreview
class FunctionsMiner(
    private val classLoader: ClassLoaderWrapper
) {

    private val logger: Logger = Logging.getLogger(this::class.java)

    /**
     * Extract all functions and properties, this will extract also functions that are defined into the supertypes
     * thise functions are all the functions that the mock class should provide
     */
    fun extractFunctionsAndProperties(kmClass: ImmutableKmClass): Pair<List<ImmutableKmFunction>, List<ImmutableKmProperty>> {
        val functions: MutableList<ImmutableKmFunction> = mutableListOf()
        val properties: MutableList<ImmutableKmProperty> = mutableListOf()
        rawExtractFunctionsAndProperties(kmClass, functions, properties)
        return functions.distinctBy { it.signature } to properties
            .filter { it.getterSignature != null || it.setterSignature != null }
            .distinctBy { it.getterSignature ?: it.getterSignature }
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
            .map { classLoader.loadClassFromDirectory(it.name).toImmutableKmClass() }
        // Functions
        val kmFunctions = kmSuperTypes.flatMap { it.functions }
        functions.addAll(kmClass.functions + kmFunctions)
        // Properties
        val kmProperties = kmSuperTypes.flatMap { it.properties }
        properties.addAll(kmClass.properties + kmProperties)

        kmSuperTypes.forEach { rawExtractFunctionsAndProperties(it, functions, properties) }
    }
}