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

package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.annotations.Mock

class KspSampleTest {

    @Mock
    val pippoMock: PippoSample = object : PippoSample {
        override val currentSession: Int
            get() = TODO("Not yet implemented")
        override var currentMutableSession: Int
            get() = TODO("Not yet implemented")
            set(value) {}

        override fun showRandom(): Boolean {
            TODO("Not yet implemented")
        }

        override fun sayHi() {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String) {
            TODO("Not yet implemented")
        }

        override fun sayHiWith(param: String, someOtherParam: Boolean) {
            TODO("Not yet implemented")
        }

        override fun sayHiWithCommonParam(param: String, intParam: Int) {
            TODO("Not yet implemented")
        }

        override suspend fun thisIsSuspend(param: String, intParam: Int) {
            TODO("Not yet implemented")
        }
    }
}

