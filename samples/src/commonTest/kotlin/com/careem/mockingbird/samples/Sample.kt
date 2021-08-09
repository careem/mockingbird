package com.careem.mockingbird.samples

import com.careem.mockingbird.PippoSampleMock
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Till we have proper gradle setup and we are able to run `generateMocks` task after building
 *
 * 1- Comment the usage of [PippoSampleMock] class
 * 2- Comment `generateMocks` tasks in build.gradle
 * 3- ./gradlew build
 * 4- ./gradlew generateMocks
 *
 *
 */
class TestClass {

    @Test
    fun main() {
        val pippoMock: PippoSample = PippoSampleMock()
        assertNotNull(pippoMock)
    }

}

