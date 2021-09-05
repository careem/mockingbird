package com.careem.mockingbird.sample

interface OuterInterface : InnerInterface {
    var yo: String
    var yoo: String
    override fun foo(fooArg: String)
    fun foo1(fooArg1: String)
    fun foo2(fooArg12: String, fooArg22: String)
}