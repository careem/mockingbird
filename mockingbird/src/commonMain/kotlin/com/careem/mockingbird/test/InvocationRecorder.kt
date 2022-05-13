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

    private val recorder = mutableMapOf<String, MutableList<Invocation>>()
    private val responses = mutableMapOf<String, LinkedHashMap<Invocation, (Invocation) -> Any?>>()

    /**
     * This function must be called by the mock when a function call is exceuted on it
     * @param uuid the uuid of the mock
     * @param invocation the Invocation object @see [Invocation]
     */
    fun storeInvocation(uuid: String, invocation: Invocation) {
        if (!recorder.containsKey(uuid)) {
            recorder[uuid] = mutableListOf()
        }
        recorder[uuid]!!.add(invocation)
    }

    /**
     * This function returns the list of invocations registered for a certain mock
     * @param uuid the uuid of the mock
     * @return list of [Invocation]s object (all the methods calls with args)
     */
    internal fun getInvocations(uuid: String): List<Invocation> {
        return recorder[uuid] ?: emptyList()
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param uuid the uuid of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param response the object that must be returned if the specif invocation happen
     */
    fun <T> storeResponse(uuid: String, invocation: Invocation, response: T) {
        val answer: (Invocation) -> T = { _ -> response }
        storeAnswer(uuid, invocation, answer)
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param uuid the uuid of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param answer the lambda that must be invoked when the invocation happen
     */
    fun <T> storeAnswer(uuid: String, invocation: Invocation, answer: (Invocation) -> T) {
        if (!responses.containsKey(uuid)) {
            responses[uuid] = LinkedHashMap()
        }
        responses[uuid]!![invocation] = answer as (Invocation) -> Any?
    }

    /**
     * This function will return the mocked response previously stored for the specific invocation
     * @param uuid the uuid of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param relaxed specify if we want to crash when no mock behavior provided
     * @throws IllegalStateException if no response was stored for the instance and invocation
     * @return the mocked response, or null if relaxed (throws if not relaxed)
     */
    fun getResponse(uuid: String, invocation: Invocation, relaxed: Boolean = false): Any? {
        return if (uuid in responses.keys) {
            responses[uuid]!!.let {
                val lambda = findResponseByInvocation(it, invocation, relaxed)
                return@let lambda(invocation)
            }
        } else if (relaxed) {
            null
        } else {
            throw IllegalStateException("Not mocked response for current object and instance, instance:$uuid, invocation: $invocation")
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
