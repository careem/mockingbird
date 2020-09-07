package com.careem.mockingbird.samples

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mck

@Mck
interface MyDependency {
    fun method1(str: String)
    fun method2(str: String, value: Int)
    fun method3(value1: Int, value2: Int): Int
    fun method4(object1: String): Int
}