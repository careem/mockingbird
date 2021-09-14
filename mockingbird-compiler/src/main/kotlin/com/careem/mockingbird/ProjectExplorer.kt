package com.careem.mockingbird

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet

class ProjectExplorer {

    private val moduleMap: MutableMap<String, Project> = mutableMapOf()
    private val logger: Logger = Logging.getLogger(this::class.java)


    fun exploreProject(rootProject: Project) {
        // TODO check sample not included here
        rootProject.traverseProjectTree()
        moduleMap.forEach { (_, it) -> println("${it.name} ${it.group} ${it::class.simpleName}") }
        println("++SubProjects++")
    }

    fun explore(project: Project): Set<Dependency> {
        val dependencySet = mutableSetOf<Dependency>()
        // TODO fix this eventually I do not want root project
        project.traverseDependencyTree(dependencySet)
        dependencySet.forEach { println("${it.name} ${it.group} ${it::class.simpleName}") }
        return dependencySet
    }

    private fun Project.traverseDependencyTree(dependencySet: MutableSet<Dependency>) {

        val kmpExtension = this.extensions.findByType(KotlinMultiplatformExtension::class.java)
        if (kmpExtension != null) {
            val sourceSets = kmpExtension.sourceSets
            val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
            println("${this.name} ${this.group}")

            val configurations =
                this.configurations.getByName(sourceSet.implementationConfigurationName).allDependencies
            dependencySet.addAll(configurations)

            // FIX me explore project and keep map so I can lookup later
            configurations.forEach {  // TODO improve performance skipping to traverse duplicated dependencies ( eg A -> B -> C and D -> B -> C do not need to explore B-> C again since I did earlier )
                moduleMap[it.name]?.traverseDependencyTree(dependencySet)
            }
        } else {
            // Container module traverse all subprojects
            this.subprojects.forEach {
                it.traverseDependencyTree(dependencySet)
            }
        }

    }

    private fun Project.traverseProjectTree() {
        subprojects.forEach {
            try {
                // Make it cleaner and better
                moduleMap[it.name] = it
                it.traverseProjectTree()
            } catch (udo: org.gradle.api.UnknownDomainObjectException) {
                logger.warn("${it.name} -> SKIPPED")
            }
        }
    }

}