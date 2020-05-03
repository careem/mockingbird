package com.careem.mockingbird.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InvocationRecorderTest {

    private val invocationRecorder = InvocationRecorder()

    @Test
    fun `test invocations stored properly for multiple instances`() {
        val mock = object : Mock {}
        val mock2 = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val invocation2 = Invocation(METHOD_2, ARGS_1)

        invocationRecorder.storeInvocation(mock, invocation1)
        invocationRecorder.storeInvocation(mock, invocation2)
        invocationRecorder.storeInvocation(mock2, invocation1)

        val mockInvocations = invocationRecorder.getInvocations(mock)
        val mock2Invocations = invocationRecorder.getInvocations(mock2)

        assertEquals(
            listOf(
                invocation1,
                invocation2
            ), mockInvocations
        )
        assertEquals(
            listOf(
                invocation1
            ), mock2Invocations
        )
    }

    @Test
    fun `test responses stored properly for multiple instances`() {
        val mock = object : Mock {}
        val mock2 = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val invocation2 = Invocation(METHOD_2, ARGS_1)
        val responseInv1 = "Yo"
        val responseInv2 = "YoYo"
        val responseInv3 = "Ahhhhhhh"

        invocationRecorder.storeResponse(mock, invocation1, responseInv1)
        invocationRecorder.storeResponse(mock, invocation2, responseInv2)
        invocationRecorder.storeResponse(mock2, invocation1, responseInv3)

        val response1 = invocationRecorder.getResponse(mock, invocation1)
        val response2 = invocationRecorder.getResponse(mock, invocation2)
        val response3 = invocationRecorder.getResponse(mock2, invocation1)

        assertEquals(responseInv1, response1)
        assertEquals(responseInv2, response2)
        assertEquals(responseInv3, response3)
    }

    @Test
    fun `test answers stored properly`() {
        val mock = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        val responseInv1 = "Yo"
        val responseAnswer1: (Invocation) -> String = { _ -> responseInv1 }

        invocationRecorder.storeAnswer(mock, invocation1, responseAnswer1)

        val response1 = invocationRecorder.getResponse(mock, invocation1)

        assertEquals(responseInv1, response1)
    }

    @Test
    fun `test get response when relaxed and unit function`() {
        val mock = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        val response = invocationRecorder.getResponse(mock, invocation1, relaxed = true)
        assertNull(response)
    }

    @Test
    fun `test get response when relaxed and not unit function`() {
        // TODO not supported
    }

    @Test
    fun `test answers side effect on unit function`() {
        val mock = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        var iWillBeSet: String? = null
        val responseAnswer1: (Invocation) -> Unit = { _ ->
            iWillBeSet = SIDE_EFFECT_VALUE
        }

        assertNull(iWillBeSet)

        invocationRecorder.storeAnswer(mock, invocation1, responseAnswer1)
        assertNull(iWillBeSet)

        invocationRecorder.getResponse(mock, invocation1)
        assertEquals(SIDE_EFFECT_VALUE, iWillBeSet)
    }

    @Test
    fun `test crash if no response store for invocation`() {
        val mock = object : Mock {}
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        var e: IllegalStateException? = null
        try {
            invocationRecorder.getResponse(mock, invocation1)
        } catch (ise: IllegalStateException) {
            e = ise
            assertEquals("Not mocked response for current object and instance", e.message)
        }
        assertNotNull(e)
    }

    @Test
    fun `test any() matcher works properly`() {
        val mock = object : Mock {}
        val mock2 = object : Mock {}
        val invocation1 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to any()))
        val invocation2 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to "value1"))
        val invocation3 = Invocation(METHOD_2, mapOf(ARG_NAME_1 to "value1", ARG_NAME_2 to any()))
        val invocation4 =
            Invocation(METHOD_2, mapOf(ARG_NAME_1 to "value1", ARG_NAME_2 to "value1"))
        val responseInv1 = "Yo"
        val responseInv2 = "YoYo"

        invocationRecorder.storeResponse(mock, invocation1, responseInv1)
        invocationRecorder.storeResponse(mock2, invocation3, responseInv2)

        val response1 = invocationRecorder.getResponse(mock, invocation2)
        val response2 = invocationRecorder.getResponse(mock2, invocation4)

        assertEquals(responseInv1, response1)
        assertEquals(responseInv2, response2)
    }

    companion object {
        private const val METHOD_1: String = "METHOD_1"
        private const val METHOD_2: String = "METHOD_2"
        private const val ARG_NAME_1: String = "ARG_NAME_1"
        private const val ARG_NAME_2: String = "ARG_NAME_2"

        private const val SIDE_EFFECT_VALUE: String = "SIDE_EFFECT_VALUE"

        private val ARGS_1 = mapOf(
            ARG_NAME_1 to "value1",
            ARG_NAME_2 to "value2"
        )
    }
}