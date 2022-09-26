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

interface JavaTypes {
    fun byte(b: Byte)
    fun shorth(s: Short)
    fun int(i: Int)
    fun long(l: Long)
    fun char(c: Char)
    fun float(f: Float)
    fun double(d: Double)
    fun boolean(b: Boolean)

    fun string(s: String)
    fun number(s: Number)
    fun obj(o: Any)
    fun comparable(o: Comparable<String>)
    fun charsequance(cs: CharSequence)
    fun throwable(t: Throwable)

    fun iterator(i: Iterator<String>)
    fun iterable(i: Iterable<String>)
    fun collection(c: Collection<String>)
    fun set(s: Set<String>)
    fun list(l: List<String>)
    fun listIterator(li: ListIterator<String>)
    fun map(m: Map<String, String>)
    fun mapEntry(me: Map.Entry<String, String>)
}