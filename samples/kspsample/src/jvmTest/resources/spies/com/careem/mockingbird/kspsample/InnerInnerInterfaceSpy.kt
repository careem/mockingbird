package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.Unit

public class InnerInnerInterfaceSpy(
  public val innerInnerInterface: InnerInnerInterface,
) : InnerInnerInterface, Spy {
  public override val uuid: String by uuid()

  public override val yo3: Int
    get() {
                  return spy(
                          methodName = Property.getYo3,
          delegate = { innerInnerInterface.yo3 }
                      )
    }

  public override fun deepFoo(deepArg: String): Unit {
                return spy(
                        methodName = Method.deepFoo,
        arguments = mapOf(Arg.deepArg to deepArg),
        delegate = { innerInnerInterface.deepFoo(deepArg) }
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
