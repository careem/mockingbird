package com.careem.mockingbird.test

data class Invocation(
    val methodName: String,
    val arguments: Map<String, Any?>
) {
    /**
     * Returns the value of argument with given [name] cast to [ArgumentType].
     *
     * @throws [kotlin.ClassCastException] if value cannot be cast to [ArgumentType].
     */
    inline fun <reified ArgumentType> getArgument(name: String): ArgumentType {
        @Suppress("UNCHECKED_CAST")
        return arguments[name] as ArgumentType
    }

    /**
     * Returns the value of argument with given subscript cast to [ArgumentType].
     *
     * @throws [kotlin.ClassCastException] if value cannot be cast to [ArgumentType].
     */
    inline operator fun <reified  ArgumentType> get(name: String): ArgumentType = getArgument(name)
}