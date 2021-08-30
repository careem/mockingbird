package com.careem.mockingbird.sample

import com.careem.mockingbird.common.sample.SampleData

interface MockWithExternalDependencies {
    fun foo(sampleData: SampleData)
}