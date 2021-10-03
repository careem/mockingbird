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

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic

object Mocks {
    internal const val TEST_STRING = "test_string"
    internal const val TEST_INT = 3

    interface MyDependency {
        fun method1(str: String)
        fun method2(str: String, value: Int)
        fun method3(value1: Int, value2: Int): Int
        fun method4(): Int
        fun method5()
        fun method6(callback: () -> Unit)
    }

    class MyDependencyMock : MyDependency, Mock {
        object Method {
            const val method1 = "method1"
            const val method2 = "method2"
            const val method3 = "method3"
            const val method4 = "method4"
            const val method5 = "method5"
            const val method6 = "method6"
        }

        object Arg {
            const val str = "str"
            const val value = "value"
            const val value1 = "value1"
            const val value2 = "value2"
            const val callback = "callback"
        }

        override fun method1(str: String) = mockUnit(
            methodName = Method.method1,
            arguments = mapOf(
                Arg.str to str
            )
        )

        override fun method2(str: String, value: Int) = mockUnit(
            methodName = Method.method2,
            arguments = mapOf(
                Arg.str to str,
                Arg.value to value
            )
        )

        override fun method3(value1: Int, value2: Int): Int = mock(
            methodName = Method.method3,
            arguments = mapOf(
                Arg.value1 to value1,
                Arg.value2 to value2
            )
        )

        override fun method4(): Int = mock(
            methodName = Method.method4
        )

        override fun method5() = mockUnit(
            methodName = Method.method5
        )

        override fun method6(callback: () -> Unit) = mockUnit(
            methodName = Method.method6,
            arguments = mapOf(
                Arg.callback to callback
            )
        )
    }

    class MyDependencySpy(private val delegate: MyDependency) : MyDependency, Spy {

        object Method {
            const val method1 = "method1"
            const val method2 = "method2"
            const val method3 = "method3"
            const val method4 = "method4"
            const val method5 = "method5"
            const val method6 = "method6"
        }

        object Arg {
            const val str = "str"
            const val value = "value"
            const val value1 = "value1"
            const val value2 = "value2"
            const val callback = "callback"
        }

        override fun method1(str: String) = spy(
            methodName = Method.method1,
            arguments = mapOf(
                Arg.str to str
            ),
            delegate = { delegate.method1(str) }
        )

        override fun method2(str: String, value: Int) = spy(
            methodName = Method.method2,
            arguments = mapOf(
                Arg.str to str,
                Arg.value to value
            ),
            delegate = { delegate.method2(str, value) }
        )

        override fun method3(value1: Int, value2: Int): Int = spy(
            methodName = Method.method3,
            arguments = mapOf(
                Arg.value1 to value1,
                Arg.value2 to value2
            ),
            delegate = { delegate.method3(value1, value2) }
        )

        override fun method4(): Int = spy(
            methodName = Method.method4,
            delegate = { delegate.method4() }
        )

        override fun method5() = spy(
            methodName = Method.method5,
            delegate = { delegate.method5() }
        )

        override fun method6(callback: () -> Unit) = spy(
            methodName = Method.method6,
            delegate = { delegate.method6(callback) },
            arguments = mapOf(
                Arg.callback to callback
            )
        )
    }

    class MyDependencyImpl : MyDependency {
        private var value: AtomicInt = atomic(0)
        override fun method1(str: String) {
            // no-ops
        }

        override fun method2(str: String, value: Int) {
            // no-ops
        }

        override fun method3(value1: Int, value2: Int): Int {
            value.value = value1 + value2
            return value.value
        }

        override fun method4(): Int {
            return value.value
        }

        override fun method5() {
            // no-ops
        }

        override fun method6(callback: () -> Unit) {
            callback()
        }
    }
}