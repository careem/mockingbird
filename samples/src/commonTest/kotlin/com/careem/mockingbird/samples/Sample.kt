package com.careem.mockingbird.samples

import com.careem.mockingbird.PippoMock
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestClass {

    @Test
    fun main() {
        val pippoMock: PippoMock = PippoMock()
        assertNotNull(pippoMock)
    }

}

