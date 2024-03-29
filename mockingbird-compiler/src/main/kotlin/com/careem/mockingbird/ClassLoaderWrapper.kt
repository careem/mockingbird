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

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.support.unzipTo
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass

@OptIn(KotlinPoetMetadataPreview::class)
class ClassLoaderWrapper(
    private val projectExplorer: ProjectExplorer,
    target: Project
) {

    private val classLoader: ClassLoader
    private val logger: Logger = Logging.getLogger(this::class.java)

    init {
        // Add all subproject to classpath TODO this can be optimized, no need to add all of them
        val urlList = mutableListOf<URL>()
        target.rootProject.traverseDependencyTree(urlList)
        extractJars(target)
        urlList.add(target.thirdPartiesClassPath().asURL())

        // Set kotlin class loader as parent in this way kotlin metadata will be loaded
        val extendedClassLoader = URLClassLoader(urlList.toTypedArray(), Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = extendedClassLoader
        classLoader = extendedClassLoader
    }


    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): KClass<*> = classLoader.loadClass(name).kotlin

    fun loadClass(kmClass: KmType): KClass<*> = loadClassFromDirectory(extractTypeString(kmClass))

    fun loadClass(kmClass: KmClass): KClass<*> = loadClassFromDirectory(kmClass.name)

    fun loadClassFromDirectory(path: String): KClass<*> {
        return loadClass(path.toJavaFullyQualifiedName())
    }

    private fun extractJars(project: Project) {
        val outputDir = File(project.thirdPartiesClassPath())
        outputDir.mkdirs()

        projectExplorer.explore(project)
            .stream()
            .filter { it is DefaultExternalModuleDependency }
            .forEach { dependency ->
                File(dependency.artifactPath(project.gradle))// TODO jvm artifcat is required right now
                    .walk()
                    .filter { file -> file.absolutePath.endsWith("${dependency.name}-jvm-${dependency.version}.jar") }
                    .toList()
                    .firstOrNull().let {
                        if (it == null) {
                            this.logger.warn("Dependency classes for ${dependency.group}:${dependency.name}:${dependency.version} not found")
                        } else {
                            unzipTo(outputDir, it)
                        }
                    }
            }
    }

    // TODO: this can be moved to ProjectExplorer
    private fun Project.traverseDependencyTree(mutableList: MutableList<URL>) {
        this.subprojects.forEach {
            mutableList.add(it.classPath().asURL())
            it.traverseDependencyTree(mutableList)
        }
    }

    private fun extractTypeString(type: KmType): String {
        return if (type.classifier is KmClassifier.Class) {
            (type.classifier as KmClassifier.Class).name
        } else {
            throw IllegalArgumentException("I can't mock this type: ${type.classifier}")
        }
    }

    private fun String.asURL(): URL =
        File(this)
            .toURI()
            .toURL()

    private fun String.toJavaFullyQualifiedName(): String = this.replace("/", ".").toJavaPackage()

    private fun String.toJavaPackage(): String {
        return when (this) {
            "kotlin.Byte" -> "java.lang.Byte"
            "kotlin.Short" -> "java.lang.Short"
            "kotlin.Int" -> "java.lang.Integer"
            "kotlin.String" -> "java.lang.String"
            "kotlin.Long" -> "java.lang.Long"
            "kotlin.Char" -> "java.lang.Character"
            "kotlin.Float" -> "java.lang.Float"
            "kotlin.Double" -> "java.lang.Double"
            "kotlin.Boolean" -> "java.lang.Boolean"

            "kotlin.Any" -> "java.lang.Object"
            "kotlin.Cloneable" -> "java.lang.Cloneable"
            "kotlin.Comparable" -> "java.lang.Comparable"
            "kotlin.Enum" -> "java.lang.Enum"
            "kotlin.CharSequence" -> "java.lang.CharSequence"
            "kotlin.Number" -> "java.lang.Number"
            "kotlin.Throwable" -> "java.lang.Throwable"

            // Collections
            "kotlin.collections.Iterator" -> "java.util.Iterator"
            "kotlin.collections.Iterable" -> "java.lang.Iterable"
            "kotlin.collections.Collection" -> "java.util.Collection"
            "kotlin.collections.Set" -> "java.util.Set"
            "kotlin.collections.List" -> "java.util.List"
            "kotlin.collections.ListIterator" -> "java.util.ListIterator"
            "kotlin.collections.Map" -> "java.util.Map"
            "kotlin.collections.Map.Entry" -> "java.util.Map\$Entry"
            // TODO arrays?
            else -> this.replace("kotlin.Function", "kotlin.jvm.functions.Function")
        }
    }
}