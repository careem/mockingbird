package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit

internal class InternalSampleInterfaceMock : InternalSampleInterface, Mock {
  public override val uuid: String by uuid()

  public override fun thisIsInternal(`param`: String): Unit {
                return mockUnit(
                        methodName = Method.thisIsInternal,
        arguments = mapOf(Arg.`param` to param)
                    )
  }

  public object Method {
    public const val thisIsInternal: String = "thisIsInternal"
  }

  public object Arg {
    public const val `param`: String = "param"
  }

  public object Property
}
