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

import co.touchlab.stately.isolate.IsolateState
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

/**
 * A interface for all capture-able class to implement so the captured value can be stored
 */
public interface Captureable {
    public fun storeCapturedValue(value: Any?)
}

/**
 * Capture any [Slot] which will be using to compare the property inside
 */
public fun <T> capture(slot: Slot<T>): CapturedMatcher<T> {
    return CapturedMatcher(slot)
}

/**
 * Capture any [Slot] which will be using to compare the property inside
 */
public fun <T> capture(list: CapturedList<T>): CapturedMatcher<T> {
    return CapturedMatcher(list)
}
public interface Slot<T> : Captureable {
    public val captured: T?
}

private class ThreadSafeSlot<T> : Slot<T> {

    private val _captured: AtomicRef<T?> = atomic(null)

    override val captured: T?
        get() = _captured.value

    @Suppress("UNCHECKED_CAST")
    override fun storeCapturedValue(value: Any?) {
        _captured.value = value as T
    }
}

private class LocalThreadSlot<T> : Slot<T> {

    private var _captured: T? = null

    override var captured: T?
        get() {
            return _captured
        }
        private set(value) {
            _captured = value
        }

    @Suppress("UNCHECKED_CAST")
    override fun storeCapturedValue(value: Any?) {
        this.captured = value as T
    }
}

@Deprecated(
    message = "Use different function call instead",
    replaceWith = ReplaceWith("slot()", "com.careem.mockingbird.test.slot")
)
/**
 * A slot using to fetch the method invocation and compare the property inside invocation arguments
 * Usage example @see [FunctionsTest]
 */
public fun <T> Slot(): Slot<T> {
    return slot()
}

/**
 * A slot using to fetch the method invocation and compare the property inside invocation arguments
 * Usage example @see [FunctionsTest]
 */
public fun <T> slot(): Slot<T> {
    return when (MockingBird.mode) {
        TestMode.MULTI_THREAD -> ThreadSafeSlot()
        TestMode.LOCAL_THREAD -> LocalThreadSlot()
    }
}
/**
 * A list that using to fetch the method invocation and compare the property inside
 * invocation arguments
 * Usage example @see [FunctionsTest]
 */
public class CapturedList<T> : Captureable {

    private val genericCaptureList = captureList<T>()
    public val captured: List<T>
        get() {
            return genericCaptureList.value
        }

    @Suppress("UNCHECKED_CAST")
    override fun storeCapturedValue(value: Any?) {
        genericCaptureList.storeCapturedValue(value)
    }
}

public interface GenericCapturedList<T> : Captureable {
    public val value: List<T>
}

private class ThreadSafeCapturedList<T> : GenericCapturedList<T> {

    private val _captured = IsolateState { mutableListOf<T>() }

    override val value: List<T>
        get() {
            return _captured.access { it.toList() }
        }

    @Suppress("UNCHECKED_CAST")
    override fun storeCapturedValue(value: Any?) {
        _captured.access { it.add(value as T) }
    }
}

public fun <T> captureList(): GenericCapturedList<T> {
    return when (MockingBird.mode) {
        TestMode.MULTI_THREAD -> ThreadSafeCapturedList()
        TestMode.LOCAL_THREAD -> LocalThreadCapturedList()
    }
}

private class LocalThreadCapturedList<T> : GenericCapturedList<T> {

    private val _captured = mutableListOf<T>()

    override val value: List<T>
        get() {
            return _captured.toList()
        }

    @Suppress("UNCHECKED_CAST")
    override fun storeCapturedValue(value: Any?) {
        this._captured.add(value as T)
    }
}

/**
 * A placeholder for where using any() as a testing matcher
 */
public class AnyMatcher

/**
 * A placeholder to indicate this argument is captured by [Slot] or [CapturedList]
 * Usage example @see [FunctionsTest]
 */
public class CapturedMatcher<T> {
    private val captureable: Captureable

    public constructor(slot: Slot<T>) {
        captureable = slot
    }

    public constructor(list: CapturedList<T>) {
        captureable = list
    }

    internal fun setCapturedValue(value: Any?) {
        captureable.storeCapturedValue(value)
    }
}