package com.careem.mockingbird.common.sample

import kotlinx.atomicfu.AtomicInt

interface ExternalDep {
    fun foo(atomicInt: AtomicInt)
}