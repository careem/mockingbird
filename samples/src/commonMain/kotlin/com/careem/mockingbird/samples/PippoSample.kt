package com.careem.mockingbird.samples

interface PippoSample {
    val currentSession: Int
    var currentMutableSession: Int
    fun showRandom(): Boolean
    fun sayHi()
    fun sayHiWith(param: String)
    fun sayHiWith(param: String, someOtherParam: Boolean)
    fun sayHiWithCommonParam(param: String, intParam: Int)
}