package com.careem.mockingbird

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClassifier
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass

@OptIn(KotlinPoetMetadataPreview::class)
class ClassLoaderWrapper(target: Project) {

    private val classLoader: ClassLoader

    init {
        // Add all subproject to classpath TODO this can be optimized, no need to add all of them
        val urlList = mutableListOf<URL>()
        traverseDependencyTree(target.rootProject, urlList)

        // Set kotlin class loader as parent in this way kotlin metadata will be loaded
        val extendedClassLoader = URLClassLoader(urlList.toTypedArray(), Thread.currentThread().contextClassLoader)
        Thread.currentThread().contextClassLoader = extendedClassLoader
        classLoader = extendedClassLoader
    }


    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): KClass<*> = classLoader.loadClass(name).kotlin
    fun loadClass(kmClass: ImmutableKmType): KClass<*> = loadClassFromDirectory(extractTypeString(kmClass))
    fun loadClass(kmClass: ImmutableKmClass): KClass<*> = loadClassFromDirectory(kmClass.name)

    fun loadClassFromDirectory(path: String): KClass<*> {
        return loadClass(path.toPackage())
    }

    private fun traverseDependencyTree(target: Project, mutableList: MutableList<URL>) {
        target.subprojects.forEach {  // TODO improve performance skipping to traverse duplicated dependencies ( eg A -> B -> C and D -> B -> C do not need to explore B-> C again since I did earlier )
            val file = File("${it.buildDir}/classes/kotlin/jvm/main")
            // Convert File to a URL
            val url = file.toURI().toURL()
            mutableList.add(url)
            traverseDependencyTree(it, mutableList)
        }
    }

    private fun String.toPackage(): String {
        return when (this) {
            "kotlin/String" -> "java.lang.String"
            "kotlin/Int" -> "java.lang.Integer"
            "kotlin/Long" -> "java.lang.Long"
            "kotlin/Boolean" -> "java.lang.Boolean"
            "kotlin/Double" -> "java.lang.Double"
            "kotlin/Float" -> "java.lang.Float"
            "kotlin/Short" -> "java.lang.Short"
            "kotlin/Char" -> "java.lang.Char"
            //TODo complete/ revise
            else -> {
                this.replace("/", ".")
            }
        }
    }

    private fun extractTypeString(type: ImmutableKmType): String {
        return if (type.classifier is KmClassifier.Class) {
            (type.classifier as KmClassifier.Class).name
        } else {
            throw IllegalArgumentException("I can't mock this type: ${type.classifier}")
        }
    }
}