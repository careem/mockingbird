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

package com.careem.mockingbird.sample

interface LambdaSample {
    fun lambda00(param: String, lambda: () -> Int)
    fun lambda01(param: String, lambda: () -> Unit)
    fun lambda10(param: String, lambda: (Int) -> Unit)
    fun lambda11(param: String, lambda: (Int) -> Double)
    fun lambda2(param: String, lambda: (String, Int) -> Unit)
    fun lambda3(param: String, lambda: (String, Int, Map<String, Int>) -> Unit)
    fun lambda4(param: String, lambda: (Boolean, Int, String, Double) -> Unit)
    fun lambda5(param: String, lambda: (Boolean, Int, String, Double, List<String>) -> Unit)
}