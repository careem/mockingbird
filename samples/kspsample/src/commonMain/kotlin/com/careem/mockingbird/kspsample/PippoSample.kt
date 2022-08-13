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

interface PippoSample {
    val currentSession: Int
    var currentMutableSession: Int
    fun showRandom(): Boolean
    fun sayHi()
    fun sayHiWith(param: String)
    fun sayHiWith(param: String, someOtherParam: Boolean)
    fun sayHiWithCommonParam(param: String, intParam: Int)
    suspend fun thisIsSuspend(param: String, intParam: Int)
}