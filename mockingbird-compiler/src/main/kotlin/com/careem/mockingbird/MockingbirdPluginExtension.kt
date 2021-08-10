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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setValue
import kotlin.reflect.KProperty

interface MockingbirdPluginExtension {
    var generateMocksFor: List<String>
    val printLogs: Boolean
}

internal class MockingbirdPluginExtensionImpl(objects: ObjectFactory) : MockingbirdPluginExtension {
    @JvmField
    internal val _generateMocksFor = objects.listProperty<String>().convention(listOf())

    @JvmField
    internal val _printLogs = objects.property<Boolean>().convention(false)

    override var generateMocksFor: List<String> by _generateMocksFor
    override var printLogs: Boolean by _printLogs // TODO implement this
}

internal inline operator fun <T> ListProperty<T>.setValue(scope: Any, property: KProperty<*>, value: List<T>) =
    set(value)

internal inline operator fun <T> ListProperty<T>.getValue(scope: Any, property: KProperty<*>): List<T> = get()