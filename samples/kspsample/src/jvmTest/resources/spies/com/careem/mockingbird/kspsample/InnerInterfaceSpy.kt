package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.Unit

public class InnerInterfaceSpy(
  public val innerInterface: InnerInterface,
) : InnerInterface, Spy {
  public override val uuid: String by uuid()

  public override var yo2: String
    get() {
                  return spy(
                          methodName = Property.getYo2,
          delegate = { innerInterface.yo2 }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setYo2,
          arguments = mapOf(Property.`value` to value),
          delegate = { innerInterface.yo2 = value }
                      )
    }

  public override val yo3: Int
    get() {
                  return spy(
                          methodName = Property.getYo3,
          delegate = { innerInterface.yo3 }
                      )
    }

  public override fun foo(fooArg: String): Unit {
                return spy(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg),
        delegate = { innerInterface.foo(fooArg) }
                    )
  }

  public override fun foo2(fooArg2: String): Unit {
                return spy(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.fooArg2 to fooArg2),
        delegate = { innerInterface.foo2(fooArg2) }
                    )
  }

  public override fun deepFoo(deepArg: String): Unit {
                return spy(
                        methodName = Method.deepFoo,
        arguments = mapOf(Arg.deepArg to deepArg),
        delegate = { innerInterface.deepFoo(deepArg) }
                    )
  }

  public object Method {
    public const val foo: String = "foo"

    public const val foo2: String = "foo2"

    public const val deepFoo: String = "deepFoo"
  }

  public object Arg {
    public const val fooArg: String = "fooArg"

    public const val fooArg2: String = "fooArg2"

    public const val deepArg: String = "deepArg"
  }

  public object Property {
    public const val getYo2: String = "getYo2"

    public const val setYo2: String = "setYo2"

    public const val getYo3: String = "getYo3"

    public const val `value`: String = "value"
  }
}
