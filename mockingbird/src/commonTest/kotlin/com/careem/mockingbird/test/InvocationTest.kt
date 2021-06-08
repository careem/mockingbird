/**
 *
 * Copyright Careem, an Uber Technologies Inc. company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.careem.mockingbird.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class InvocationTest {
    @Test
    fun castPresentArgumentToType() {
        val arg: Int = invocation["bar"]

        assertEquals(10, arg)
    }

    @Test
    fun castAbsentArgumentToNullableType() {
        val arg: Int? = invocation["gibberish"]

        assertNull(arg)
    }

    @Test
    fun failToCastPresentArgumentToType() {
        assertFailsWith(ClassCastException::class) {
            invocation.getArgument<String>("bar")
        }
    }

    private companion object {
        val invocation = Invocation(
            methodName = "foo",
            arguments = mapOf(
                "bar" to 10
            )
        )
    }
}