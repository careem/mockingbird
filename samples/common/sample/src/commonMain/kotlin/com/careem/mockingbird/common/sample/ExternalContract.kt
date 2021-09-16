package com.careem.mockingbird.common.sample

import com.badoo.reaktive.disposable.Disposable

interface ExternalContract {
    fun fx(deepSample: DeepSampleData)
    fun fx2(disposable: Disposable)
}