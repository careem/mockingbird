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

import kotlin.test.*

class InvocationRecorderTest {

    private val invocationRecorder = InvocationRecorder()

    @Test
    fun testEmptyListWhenNoInvocationsRegisteredForAnInstance(){
        val mock = newMock()

        val mockInvocations = invocationRecorder.getInvocations(mock.uuid)
        assertTrue(mockInvocations.isEmpty())
    }

    @Test
    fun testInvocationsStoredProperlyForMultipleInstances() {
        val mock = newMock()
        val mock2 = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val invocation2 = Invocation(METHOD_2, ARGS_1)

        invocationRecorder.storeInvocation(mock.uuid, invocation1)
        invocationRecorder.storeInvocation(mock.uuid, invocation2)
        invocationRecorder.storeInvocation(mock2.uuid, invocation1)

        val mockInvocations = invocationRecorder.getInvocations(mock.uuid)
        val mock2Invocations = invocationRecorder.getInvocations(mock2.uuid)

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
    fun testUuidGeneration() {
        val mock = Mocks.MyDependencyMock()
        val uuid = mock.uuid
        assertEquals(uuid, mock.uuid)
        assertEquals(uuid, mock.uuid)
        assertEquals(uuid, mock.uuid)
        assertEquals(uuid, mock.uuid)

        val mock2 = Mocks.MyDependencyMock()
        val uuid2 = mock2.uuid
        assertEquals(uuid2, mock2.uuid)
        assertEquals(uuid2, mock2.uuid)
        assertEquals(uuid2, mock2.uuid)
        assertEquals(uuid2, mock2.uuid)

        println(uuid)
        println(uuid2)

        assertNotEquals(uuid, uuid2)
    }

    @Test
    fun testInvocationsStoredProperlyForMultipleInstancesOfSameMock() {
        val iterations = 100000
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val uuid = mutableSetOf<String>()

        (0 until iterations).forEach {
            val mock = Mocks.MyDependencyMock()
            uuid.add(mock.uuid)
            invocationRecorder.storeInvocation(mock.uuid, invocation1)
        }

        assertEquals(iterations, uuid.size)
        uuid.forEach {
            assertEquals(1, invocationRecorder.getInvocations(it).size, message = "Key: $it")
        }
    }

    @Test
    fun testResponsesStoredProperlyForMultipleInstances() {
        val mock = newMock()
        val mock2 = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val invocation2 = Invocation(METHOD_2, ARGS_1)
        val responseInv1 = "Yo"
        val responseInv2 = "YoYo"
        val responseInv3 = "Ahhhhhhh"

        invocationRecorder.storeResponse(mock.uuid, invocation1, responseInv1)
        invocationRecorder.storeResponse(mock.uuid, invocation2, responseInv2)
        invocationRecorder.storeResponse(mock2.uuid, invocation1, responseInv3)

        val response1 = invocationRecorder.getResponse(mock.uuid, invocation1)
        val response2 = invocationRecorder.getResponse(mock.uuid, invocation2)
        val response3 = invocationRecorder.getResponse(mock2.uuid, invocation1)

        assertEquals(responseInv1, response1)
        assertEquals(responseInv2, response2)
        assertEquals(responseInv3, response3)
    }

    @Test
    fun testAnswersStoredProperly() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        val responseInv1 = "Yo"
        val responseAnswer1: (Invocation) -> String = { _ -> responseInv1 }

        invocationRecorder.storeAnswer(mock.uuid, invocation1, responseAnswer1)

        val response1 = invocationRecorder.getResponse(mock.uuid, invocation1)

        assertEquals(responseInv1, response1)
    }

    @Test
    fun testGetResponseWhenRelaxedAndUnitFunction() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        val response = invocationRecorder.getResponse(mock.uuid, invocation1, relaxed = true)
        assertNull(response)
    }

    @Test
    fun testGetResponseWhenRelaxedAndNotUnitFunction() {
        // TODO not supported
    }

    @Test
    fun testAnswersSideEffectOnUnitFunction() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        var iWillBeSet: String? = null
        val responseAnswer1: (Invocation) -> Unit = { _ ->
            iWillBeSet = SIDE_EFFECT_VALUE
        }

        assertNull(iWillBeSet)

        invocationRecorder.storeAnswer(mock.uuid, invocation1, responseAnswer1)
        assertNull(iWillBeSet)

        invocationRecorder.getResponse(mock.uuid, invocation1)
        assertEquals(SIDE_EFFECT_VALUE, iWillBeSet)
    }

    @Test
    fun testCrashIfNoResponseStoreForInvocation() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)

        var e: IllegalStateException? = null
        try {
            invocationRecorder.getResponse(mock.uuid, invocation1)
        } catch (ise: IllegalStateException) {
            e = ise
            assertEquals("Not mocked response for current object and instance, instance:${mock.uuid}, invocation: $invocation1", e.message)
        }
        assertNotNull(e)
    }

    @Test
    fun testCrashIfResponseStoredButWrongArgumentSize() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, ARGS_1)
        val invocation2 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to "value1"))
        val responseInv1 = "Yo"
        var e: IllegalStateException? = null

        invocationRecorder.storeResponse(mock.uuid, invocation1, responseInv1)

        val response1 = invocationRecorder.getResponse(mock.uuid, invocation1)
        assertEquals(responseInv1, response1)
        try {
            invocationRecorder.getResponse(mock.uuid, invocation2)
        } catch (ise: IllegalStateException) {
            e = ise
            assertEquals("Not mocked response for current object and instance, invocation: $invocation2", e.message)
        }
        assertNotNull(e)
    }

    @Test
    fun testCrashIfResponseStoredButWrongArgumentKey() {
        val mock = newMock()
        val invocation1 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to "value1"))
        val invocation2 = Invocation(METHOD_1, mapOf("wrong_key" to "value1"))
        val responseInv1 = "Yo"
        var e: IllegalStateException? = null

        invocationRecorder.storeResponse(mock.uuid, invocation1, responseInv1)

        val response1 = invocationRecorder.getResponse(mock.uuid, invocation1)
        assertEquals(responseInv1, response1)
        try {
            invocationRecorder.getResponse(mock.uuid, invocation2)
        } catch (ise: IllegalStateException) {
            e = ise
            assertEquals("Not mocked response for current object and instance, invocation: $invocation2", e.message)
        }
        assertNotNull(e)
    }

    @Test
    fun testAnyMatcherWorksProperly() {
        val mock = newMock()
        val mock2 = newMock()
        val invocation1 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to any()))
        val invocation2 = Invocation(METHOD_1, mapOf(ARG_NAME_1 to "value1"))
        val invocation3 = Invocation(METHOD_2, mapOf(ARG_NAME_1 to "value1", ARG_NAME_2 to any()))
        val invocation4 =
            Invocation(METHOD_2, mapOf(ARG_NAME_1 to "value1", ARG_NAME_2 to "value1"))
        val responseInv1 = "Yo"
        val responseInv2 = "YoYo"

        invocationRecorder.storeResponse(mock.uuid, invocation1, responseInv1)
        invocationRecorder.storeResponse(mock2.uuid, invocation3, responseInv2)

        val response1 = invocationRecorder.getResponse(mock.uuid, invocation2)
        val response2 = invocationRecorder.getResponse(mock2.uuid, invocation4)

        assertEquals(responseInv1, response1)
        assertEquals(responseInv2, response2)
    }

    fun newMock(): Mock{
        return object : Mock {
            override val uuid: String by uuid()
        }
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