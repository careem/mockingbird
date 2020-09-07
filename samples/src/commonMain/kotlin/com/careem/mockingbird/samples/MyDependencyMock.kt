package com.careem.mockingbird.samples

//import com.careem.mockingbird.test.Mock
//import com.careem.mockingbird.test.mock
//import com.careem.mockingbird.test.mockUnit
//
//class MyDependencyMock : MyDependency {
//    object Method {
//        const val method1 = "method1"
//        const val method2 = "method2"
//        const val method3 = "method3"
//        const val method4 = "method4"
//    }
//
//    object Arg {
//        const val str = "str"
//        const val value = "value"
//        const val value1 = "value1"
//        const val value2 = "value2"
//        const val object1 = "object1"
//    }
//
//    override fun method1(str: String) = mockUnit(
//        methodName = Method.method1,
//        arguments = mapOf(
//            Arg.str to str
//        )
//    )
//
//    override fun method2(str: String, value: Int) = mockUnit(
//        methodName = Method.method2,
//        arguments = mapOf(
//            Arg.str to str,
//            Arg.value to value
//        )
//    )
//
//    override fun method3(value1: Int, value2: Int): Int = mock(
//        methodName = Method.method3,
//        arguments = mapOf(
//            Arg.value1 to value1,
//            Arg.value2 to value2
//        )
//    )
//
//    override fun method4(object1: String): Int = mock(
//        methodName = Method.method4,
//        arguments = mapOf(
//            Arg.object1 to object1
//        )
//    )
//}