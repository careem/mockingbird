package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.Throwable
import kotlin.Unit

public class OuterInterfaceSpy(
  public val outerInterface: OuterInterface,
) : OuterInterface, Spy {
  public override val uuid: String by uuid()

  public override var yo: String
    get() {
                  return spy(
                          methodName = Property.getYo,
          delegate = { outerInterface.yo }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setYo,
          arguments = mapOf(Property.`value` to value),
          delegate = { outerInterface.yo = value }
                      )
    }

  public override var yoo: String
    get() {
                  return spy(
                          methodName = Property.getYoo,
          delegate = { outerInterface.yoo }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setYoo,
          arguments = mapOf(Property.`value` to value),
          delegate = { outerInterface.yoo = value }
                      )
    }

  public override var yo2: String
    get() {
                  return spy(
                          methodName = Property.getYo2,
          delegate = { outerInterface.yo2 }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setYo2,
          arguments = mapOf(Property.`value` to value),
          delegate = { outerInterface.yo2 = value }
                      )
    }

  public override val yo3: Int
    get() {
                  return spy(
                          methodName = Property.getYo3,
          delegate = { outerInterface.yo3 }
                      )
    }

  public override fun foo1(fooArg1: String): Unit {
                return spy(
                        methodName = Method.foo1,
        arguments = mapOf(Arg.fooArg1 to fooArg1),
        delegate = { outerInterface.foo1(fooArg1) }
                    )
  }

  public override fun foo2(fooArg12: String, fooArg22: String): Unit {
                return spy(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.fooArg12 to fooArg12,Arg.fooArg22 to fooArg22),
        delegate = { outerInterface.foo2(fooArg12,fooArg22) }
                    )
  }

  public override fun thr(throwable: Throwable): Unit {
                return spy(
                        methodName = Method.thr,
        arguments = mapOf(Arg.throwable to throwable),
        delegate = { outerInterface.thr(throwable) }
                    )
  }

  public override fun foo(fooArg: String): Unit {
                return spy(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg),
        delegate = { outerInterface.foo(fooArg) }
                    )
  }

  public override fun foo2(fooArg2: String): Unit {
                return spy(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.fooArg2 to fooArg2),
        delegate = { outerInterface.foo2(fooArg2) }
                    )
  }

  public override fun deepFoo(deepArg: String): Unit {
                return spy(
                        methodName = Method.deepFoo,
        arguments = mapOf(Arg.deepArg to deepArg),
        delegate = { outerInterface.deepFoo(deepArg) }
                    )
  }

  public object Method {
    public const val foo1: String = "foo1"

    public const val foo2: String = "foo2"

    public const val thr: String = "thr"

    public const val foo: String = "foo"

    public const val deepFoo: String = "deepFoo"
  }

  public object Arg {
    public const val fooArg1: String = "fooArg1"

    public const val fooArg12: String = "fooArg12"

    public const val fooArg22: String = "fooArg22"

    public const val throwable: String = "throwable"

    public const val fooArg: String = "fooArg"

    public const val fooArg2: String = "fooArg2"

    public const val deepArg: String = "deepArg"
  }

  public object Property {
    public const val getYo: String = "getYo"

    public const val setYo: String = "setYo"

    public const val getYoo: String = "getYoo"

    public const val setYoo: String = "setYoo"

    public const val getYo2: String = "getYo2"

    public const val setYo2: String = "setYo2"

    public const val getYo3: String = "getYo3"

    public const val `value`: String = "value"
  }
}
