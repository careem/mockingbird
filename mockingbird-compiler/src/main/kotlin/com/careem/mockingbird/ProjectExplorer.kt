package com.careem.mockingbird

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet

/**
 * Project explorer, this class will perform all the exploration logic to determine the project structure and its dependencies
 * NOTE: for project here we mean the target project of the plugin not the root one, we refer to the root one with the name rootProject
 */
class ProjectExplorer {

    private val moduleMap: MutableMap<String, Project> = mutableMapOf()
    private val isExplored: HashSet<String> = hashSetOf()
    private val logger: Logger = Logging.getLogger(this::class.java)

    fun visitRootProject(rootProject: Project) {
        rootProject.traverseProjectTree()
    }

    fun explore(project: Project): Set<Dependency> {
        val dependencySet = mutableSetOf<Dependency>()
        // TODO fix this eventually I do not want root project
        project.traverseDependencyTree(dependencySet)
        return dependencySet
    }

    private fun Project.traverseDependencyTree(dependencySet: MutableSet<Dependency>) {
        if (!isExplored.contains(this.name)) {
            val kmpExtension = this.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kmpExtension != null) {
                val sourceSets = kmpExtension.sourceSets
                val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)

                val configurations =
                    this.configurations.getByName(sourceSet.implementationConfigurationName).allDependencies
                dependencySet.addAll(configurations)

                configurations.forEach {
                    moduleMap[it.name]?.traverseDependencyTree(dependencySet)
                }
            } else {
                // Container module traverse all subprojects
                this.subprojects.forEach {
                    it.traverseDependencyTree(dependencySet)
                }
            }
        }
        isExplored.add(this.name)
    }

    private fun Project.traverseProjectTree() {
        subprojects.forEach {
            try {
                moduleMap[it.name] = it
                it.traverseProjectTree()
            } catch (udo: org.gradle.api.UnknownDomainObjectException) {
                logger.warn("${it.name} -> SKIPPED")
            }
        }
    }

}