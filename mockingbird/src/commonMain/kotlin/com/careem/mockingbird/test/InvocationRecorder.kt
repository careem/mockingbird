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

internal class InvocationRecorder {

    init {
        ensureNeverFrozen()
    }

    private val recorder = mutableMapOf<Int, MutableList<Invocation>>()
    private val responses = mutableMapOf<Int, LinkedHashMap<Invocation, (Invocation) -> Any?>>()

    /**
     * This function must be called by the mock when a function call is exceuted on it
     * @param instanceHash the instanceHash of the mock
     * @param invocation the Invocation object @see [Invocation]
     */
    fun storeInvocation(instanceHash: Int, invocation: Invocation) {
        if (!recorder.containsKey(instanceHash)) {
            recorder[instanceHash] = mutableListOf()
        }
        recorder[instanceHash]!!.add(invocation)
    }

    /**
     * This function returns the list of invocations registered for a certain mock
     * @param instanceHash the instanceHash of the mock
     * @return list of [Invocation]s object (all the methods calls with args)
     */
    internal fun getInvocations(instanceHash: Int): List<Invocation> {
        return recorder[instanceHash] ?: emptyList()
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param instanceHash the instanceHash of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param response the object that must be returned if the specif invocation happen
     */
    fun <T> storeResponse(instanceHash: Int, invocation: Invocation, response: T) {
        val answer: (Invocation) -> T = { _ -> response }
        storeAnswer(instanceHash, invocation, answer)
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param instanceHash the instanceHash of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param answer the lambda that must be invoked when the invocation happen
     */
    fun <T> storeAnswer(instanceHash: Int, invocation: Invocation, answer: (Invocation) -> T) {
        if (!responses.containsKey(instanceHash)) {
            responses[instanceHash] = LinkedHashMap()
        }
        responses[instanceHash]!![invocation] = answer as (Invocation) -> Any?
    }

    /**
     * This function will return the mocked response previously stored for the specific invocation
     * @param instanceHash the instanceHash of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param relaxed specify if we want to crash when no mock behavior provided
     * @throws IllegalStateException if no response was stored for the instance and invocation
     * @return the mocked response, or null if relaxed (throws if not relaxed)
     */
    fun getResponse(instanceHash: Int, invocation: Invocation, relaxed: Boolean = false): Any? {
        return if (instanceHash in responses.keys) {
            responses[instanceHash]!!.let {
                val lambda = findResponseByInvocation(it, invocation, relaxed)
                return@let lambda(invocation)
            }
        } else if (relaxed) {
            null
        } else {
            throw IllegalStateException("Not mocked response for current object and instance, instance:$instanceHash, invocation: $invocation")
        }
    }

    /**
     * Helper to find stores response, need to take care of @see [AnyMatcher]
     */
    private fun findResponseByInvocation(
        storedInvocationMap: Map<Invocation, (Invocation) -> Any?>,
        invocation: Invocation,
        relaxed: Boolean
    ): ((Invocation) -> Any?) {
        for (storedInvocation in storedInvocationMap.keys.reversed()) {
            if (compareInvocation(storedInvocation, invocation)) {
                return storedInvocationMap[storedInvocation]!!
            }
        }
        if (relaxed) {
            return { null }
        } else {
            throw IllegalStateException("Not mocked response for current object and instance, invocation: $invocation")
        }
    }

    /**
     * Helper to compare invocation and any() matcher
     */
    private fun compareInvocation(
        storedInvocation: Invocation,
        actualInvocation: Invocation
    ): Boolean {
        if (storedInvocation.methodName != actualInvocation.methodName) {
            return false
        } else if (storedInvocation.arguments.size != actualInvocation.arguments.size) {
            return false
        }
        for (key in storedInvocation.arguments.keys) {
            if (!actualInvocation.arguments.containsKey(key)) {
                return false
            } else if (storedInvocation.arguments[key] !is AnyMatcher &&
                storedInvocation.arguments[key] != actualInvocation.arguments[key]
            ) {
                return false
            }
        }
        return true
    }
}
