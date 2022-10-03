package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.Unit

public class InnerInnerInterfaceMock : InnerInnerInterface, Mock {
  public override val uuid: String by uuid()

  public override val yo3: Int
    get() = mock(
        methodName = Property.getYo3
    )

  public override fun deepFoo(deepArg: String): Unit {
                return mockUnit(
                        methodName = Method.deepFoo,
        arguments = mapOf(Arg.deepArg to deepArg)
                    )
  }

  public object Method {
    public const val deepFoo: String = "deepFoo"
  }

  public object Arg {
    public const val deepArg: String = "deepArg"
  }

  public object Property {
    public const val getYo3: String = "getYo3"
  }
}
