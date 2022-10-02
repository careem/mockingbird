package com.careem.mockingbird.kspsample

import com.careem.mockingbird.common.sample.SampleData
import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit

public class MockWithExternalDependenciesMock : MockWithExternalDependencies, Mock {
  public override val uuid: String by uuid()

  public override fun foo(sampleData: SampleData): Unit {
                return mockUnit(
                        methodName = Method.foo,
        arguments = mapOf(Arg.sampleData to sampleData)
                    )
  }

  public object Method {
    public const val foo: String = "foo"
  }

  public object Arg {
    public const val sampleData: String = "sampleData"
  }

  public object Property
}
