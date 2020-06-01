package com.careem.mockingbird.test

internal class InvocationRecorder {

    init {
        ensureNeverFrozen()
    }

    private val recorder = mutableMapOf<Int, MutableList<Invocation>>()
    private val responses = mutableMapOf<Int, MutableMap<Invocation, (Invocation) -> Any?>>()

    /**
     * This function must be called by the mock when a function call is exceuted on it
     * @param instance the instance of the mock
     * @param invocation the Invocation object @see [Invocation]
     */
    fun storeInvocation(instance: Any, invocation: Invocation) {
        val instanceHash = instance.hashCode()
        if (!recorder.containsKey(instanceHash)) {
            recorder[instanceHash] = mutableListOf()
        }
        recorder[instanceHash]!!.add(invocation)
    }

    /**
     * This function returns the list of invocations registered for a certain mock
     * @param instance the instance of the mock
     * @return list of [Invocation]s object (all the methods calls with args)
     */
    internal fun getInvocations(instance: Any): List<Invocation> {
        val instanceHash = instance.hashCode()
        return recorder[instanceHash] ?: emptyList()
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param instance the instance of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param response the object that must be returned if the specif invocation happen
     */
    fun <T> storeResponse(instance: Any, invocation: Invocation, response: T) {
        val answer: (Invocation) -> T = { _ -> response }
        storeAnswer(instance, invocation, answer)
    }

    /**
     * This function tells to InvocationRecorder how to reply if invocation is received
     * @param instance the instance of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param answer the lambda that must be invoked when the invocation happen
     */
    fun <T> storeAnswer(instance: Any, invocation: Invocation, answer: (Invocation) -> T) {
        val instanceHash = instance.hashCode()
        if (!responses.containsKey(instanceHash)) {
            responses[instanceHash] = mutableMapOf()
        }
        responses[instanceHash]!![invocation] = answer as (Invocation) -> Any?
    }

    /**
     * This function will return the mocked response previously stored for the specific invocation
     * @param instance the instance of the mock
     * @param invocation the Invocation object @see [Invocation]
     * @param relaxed specify if we want to crash when no mock behavior provided
     * @throws IllegalStateException if no response was stored for the instance and invocation
     * @return the mocked response, or null if relaxed (throws if not relaxed)
     */
    fun getResponse(instance: Any, invocation: Invocation, relaxed: Boolean = false): Any? {
        val instanceHash = instance.hashCode()
        return if (instanceHash in responses.keys) {
            responses[instanceHash]!!.let {
                val lambda = findResponseByInvocation(it, invocation, relaxed)
                return@let lambda(invocation)
            }
        } else if (relaxed) {
            null
        } else {
            throw IllegalStateException("Not mocked response for current object and instance")
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
        for (storedInvocation in storedInvocationMap.keys) {
            if (compareInvocation(storedInvocation, invocation)) {
                return storedInvocationMap[storedInvocation]!!
            }
        }
        if (relaxed) {
            return { null }
        } else {
            throw IllegalStateException("Not mocked response for current object and instance")
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
