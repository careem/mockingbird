package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.Throwable
import kotlin.Unit

public class OuterInterfaceMock : OuterInterface, Mock {
  public override val uuid: String by uuid()

  public override var yo: String
    get() = mock(
        methodName = Property.getYo
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setYo,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override var yoo: String
    get() = mock(
        methodName = Property.getYoo
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setYoo,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override var yo2: String
    get() = mock(
        methodName = Property.getYo2
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setYo2,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override val yo3: Int
    get() = mock(
        methodName = Property.getYo3
    )

  public override fun foo1(fooArg1: String): Unit {
                return mockUnit(
                        methodName = Method.foo1,
        arguments = mapOf(Arg.fooArg1 to fooArg1)
                    )
  }

  public override fun foo2(fooArg12: String, fooArg22: String): Unit {
                return mockUnit(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.fooArg12 to fooArg12,Arg.fooArg22 to fooArg22)
                    )
  }

  public override fun thr(throwable: Throwable): Unit {
                return mockUnit(
                        methodName = Method.thr,
        arguments = mapOf(Arg.throwable to throwable)
                    )
  }

  public override fun foo(fooArg: String): Unit {
                return mockUnit(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg)
                    )
  }

  public override fun foo2(fooArg2: String): Unit {
                return mockUnit(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.fooArg2 to fooArg2)
                    )
  }

  public override fun deepFoo(deepArg: String): Unit {
                return mockUnit(
                        methodName = Method.deepFoo,
        arguments = mapOf(Arg.deepArg to deepArg)
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
