package com.careem.mockingbird.kspsample

import com.careem.mockingbird.common.sample.SampleData
import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit

public class MockWithExternalDependenciesSpy(
  public val mockWithExternalDependencies: MockWithExternalDependencies,
) : MockWithExternalDependencies, Spy {
  public override val uuid: String by uuid()

  public override fun foo(sampleData: SampleData): Unit {
                return spy(
                        methodName = Method.foo,
        arguments = mapOf(Arg.sampleData to sampleData),
        delegate = { mockWithExternalDependencies.foo(sampleData) }
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
