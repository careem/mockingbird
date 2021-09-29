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

package com.careem.mockingbird.test

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockingBirdTest {

    @BeforeTest
    fun setUp() {
        MockingBird.reset()
    }

    @Test
    fun testSetModeToSingleThread() {
        MockingBird.mode = TestMode.LOCAL_THREAD

        assertEquals(TestMode.LOCAL_THREAD, MockingBird.mode)
        assertTrue(MockingBird.invocationRecorder() is SimpleInvocationRecorderProvider)
    }

    @Test
    fun testSetModeToMultiThread() {
        MockingBird.mode = TestMode.MULTI_THREAD

        assertEquals(TestMode.MULTI_THREAD, MockingBird.mode)
        assertTrue(MockingBird.invocationRecorder() is IsolateStateInvocationRecorderProvider)
    }

    @Test
    fun testDefaultTestModeWhenNoSet() {
        assertEquals(TestMode.LOCAL_THREAD, MockingBird.mode)
        assertTrue(MockingBird.invocationRecorder() is SimpleInvocationRecorderProvider)
    }


    @Test
    fun testMultipleSetBeforeMockInteraction() {
        MockingBird.mode = TestMode.MULTI_THREAD
        MockingBird.mode = TestMode.LOCAL_THREAD

        assertEquals(TestMode.LOCAL_THREAD, MockingBird.mode)
        assertTrue(MockingBird.invocationRecorder() is SimpleInvocationRecorderProvider)
    }

    @Test
    fun testMultipleSetAfterMockInteraction() {
        val testMock = Mocks.MyDependencyMock()
        testMock.every(
            methodName = Mocks.MyDependencyMock.Method.method3,
            arguments = mapOf(
                Mocks.MyDependencyMock.Arg.value1 to Mocks.TEST_INT,
                Mocks.MyDependencyMock.Arg.value2 to Mocks.TEST_INT
            )
        ) { Mocks.TEST_INT }

        try {
            MockingBird.mode = TestMode.MULTI_THREAD
        } catch (error: UnsupportedOperationException) {
            assertNotNull(error)
        } finally {
            assertEquals(TestMode.LOCAL_THREAD, MockingBird.mode)
        }
    }

    @AfterTest
    fun tearDown() {
        MockingBird.reset()
    }
}