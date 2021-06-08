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
    inline operator fun <reified ArgumentType> get(name: String): ArgumentType = getArgument(name)
}
