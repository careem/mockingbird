package com.careem.mockingbird

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClassifier
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
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

    init {
        // Add all subproject to classpath TODO this can be optimized, no need to add all of them
        val urlList = mutableListOf<URL>()
        target.rootProject.traverseDependencyTree(urlList)
        extractJars(target)
        urlList.add((target.buildDir.absolutePath + File.separator + "dependencies").asURL())

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
        return loadClass(path.toJavaFullyQualifiedName())
    }

    private fun extractJars(project: Project) {
        val outputDir =
            File(project.buildDir.absolutePath + File.separator + "dependencies")
        outputDir.mkdirs()

        val dependencySet = projectExplorer.explore(project) // TODO avoid recomputing this
        dependencySet.stream()
            .filter { it is DefaultExternalModuleDependency }
            .forEach { dependency ->
                val depRoot =
                    File(project.gradle.gradleUserHomeDir.absolutePath + File.separator + "caches/modules-2/files-2.1" + File.separator + dependency.group + File.separator + dependency.name + "-jvm" + File.separator + dependency.version)// TODO jvm is required right now
                depRoot.walk()
                    .filter { file -> file.absolutePath.endsWith("${dependency.name}-jvm-${dependency.version}.jar") }
                    .toList()
                    .firstOrNull()?.let {
                        unzipTo(outputDir, it)
                    } // TODO only 1 should be here
            }
    }

    private fun Project.traverseDependencyTree(mutableList: MutableList<URL>) {
        // TODO Revist this not sure it is needed anymore
        this.subprojects.forEach {  // TODO improve performance skipping to traverse duplicated dependencies ( eg A -> B -> C and D -> B -> C do not need to explore B-> C again since I did earlier )
            mutableList.add("${it.buildDir}/classes/kotlin/jvm/main".asURL())
            it.traverseDependencyTree(mutableList)
        }
    }

    private fun String.asURL(): URL {
        val file = File(this)
        return file.toURI().toURL()
    }

    private fun String.toJavaFullyQualifiedName(): String {
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