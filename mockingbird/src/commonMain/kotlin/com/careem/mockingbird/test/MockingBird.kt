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

import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

@SharedImmutable
private val mtInvocationRecorder = IsolateStateInvocationRecorderProvider()

@ThreadLocal
private val localInvocationRecorder = SimpleInvocationRecorderProvider()


public enum class TestMode {
    MULTI_THREAD, LOCAL_THREAD
}

internal object MockingBird {
    private val DEFAULT_TEST_MODE = TestMode.MULTI_THREAD

    private val _mode = atomic(DEFAULT_TEST_MODE)
    private val canChangeMode = atomic(true)
    internal var mode: TestMode
        get() = _mode.value
        set(value) {
            if (!canChangeMode.value) throw UnsupportedOperationException("Test mode cannot be changed after mock interaction")
            _mode.value = value
        }

    /**
     * Reset the test mode configuration, this function should be called on the @After function
     */
    internal fun reset() {
        _mode.value = DEFAULT_TEST_MODE
        canChangeMode.value = true
    }

    internal fun invocationRecorder(): InvocationRecorderProvider {
        // the mode must be chosen before any mock once the mode has been chosen and any operation on the mock is executed
        // you cannot change the mode anymore
        canChangeMode.value = false
        return when (mode) {
            TestMode.LOCAL_THREAD -> localInvocationRecorder
            TestMode.MULTI_THREAD -> mtInvocationRecorder
        }
    }

}