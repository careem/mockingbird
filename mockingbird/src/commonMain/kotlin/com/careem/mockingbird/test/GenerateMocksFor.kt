package com.careem.mockingbird.test

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class GenerateMocksFor(vararg val mocks: KClass<*>)
