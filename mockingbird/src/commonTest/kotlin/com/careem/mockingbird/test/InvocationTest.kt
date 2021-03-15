package com.careem.mockingbird.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class InvocationTest {
    @Test
    fun castPresentArgumentToType() {
        val arg: Int = invocation.getArgument("bar")

        assertEquals(10, arg)
    }

    @Test
    fun castAbsentArgumentToNullableType() {
        val arg: Int? = invocation.getArgument("gibberish")

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