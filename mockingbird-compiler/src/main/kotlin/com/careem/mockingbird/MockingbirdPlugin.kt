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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.get

internal const val EXTENSION_NAME = "mockingBird"

abstract class MockingbirdPlugin : Plugin<Project> {

    private val mockingbirdPluginLegacyCodeGenDelegate = MockingbirdPluginLegacyCodeGenDelegate()
    private val mockingbirdPluginKspDelegate = MockingbirdPluginKspDelegate()

    private val logger: Logger = Logging.getLogger(this::class.java)

    override fun apply(target: Project) {
        target.extensions.add<MockingbirdPluginExtension>(
            EXTENSION_NAME, MockingbirdPluginExtensionImpl(target.objects)
        )
        if (legacyCodeGenRequired(target)) {
            mockingbirdPluginLegacyCodeGenDelegate.apply(target)
        }
        mockingbirdPluginKspDelegate.apply(target)
    }

    private fun legacyCodeGenRequired(target: Project): Boolean {
        val pluginExtensions = target.extensions[EXTENSION_NAME] as MockingbirdPluginExtensionImpl
        val legacyCodeGenRequested = pluginExtensions.generateMocksFor.isNotEmpty()
        logger.info("LegacyCodeGen: $legacyCodeGenRequested")
        return legacyCodeGenRequested
    }
}




