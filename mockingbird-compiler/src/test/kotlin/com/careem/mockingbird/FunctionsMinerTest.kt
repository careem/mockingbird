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
import com.squareup.kotlinpoet.metadata.toKmClass
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(KotlinPoetMetadataPreview::class)
class FunctionsMinerTest {

    private val classLoaderWrapper = mockk<ClassLoaderWrapper>()
    private lateinit var functionsMiner: FunctionsMiner

    @Before
    fun setup() {
        functionsMiner = FunctionsMiner(classLoaderWrapper)
    }

    @Test
    fun testExtractFunctionsAndPropertiesWithSuperInterface() {
        val (functions, properties) = functionsMiner.extractFunctionsAndProperties(SuperInterface::class.toKmClass())
        assertEquals(2, functions.size)
        assertEquals(0, properties.size)
    }

    @Test
    fun testExtractFunctionsAndPropertiesWithChildInterface() {
        every { classLoaderWrapper.loadClassFromDirectory(any()) } returns SuperInterface::class
        val (functions, properties) = functionsMiner.extractFunctionsAndProperties(ChildInterface::class.toKmClass())
        assertEquals(4, functions.size)
        assertEquals(0, properties.size)
    }

    @Test
    fun testExtractFunctionsAndPropertiesWithChildInterfaceImpl() {
        every { classLoaderWrapper.loadClassFromDirectory(SuperInterface::class.toKmClass().name) } returns SuperInterface::class
        every { classLoaderWrapper.loadClassFromDirectory(ChildInterface::class.toKmClass().name) } returns ChildInterface::class
        val (functions, properties) = functionsMiner.extractFunctionsAndProperties(ChildInterfaceImpl::class.toKmClass())
        assertEquals(4, functions.size)
        assertEquals(2, properties.size)
    }

    private interface SuperInterface {
        fun method1(): String
        fun method2(str: String): Int
    }

    private interface ChildInterface : SuperInterface {
        fun method3(): Boolean
        fun method4(str: String): Long
    }

    private class ChildInterfaceImpl : ChildInterface {
        val property1 = "1"
        val property2 = 2
        private val property3 = false
        private val property4 = 0L

        override fun method1(): String = property1
        override fun method2(str: String): Int = property2
        override fun method3(): Boolean = property3
        override fun method4(str: String): Long = property4
    }

}