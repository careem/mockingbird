package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public class InterfaceWithGenericsMock : InterfaceWithGenerics, Mock {
  public override val uuid: String by uuid()

  public override var g1: List<String>
    get() = mock(
        methodName = Property.getG1
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setG1,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override var g2: Map<String, Int>
    get() = mock(
        methodName = Property.getG2
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setG2,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override fun complexFoo(fooArg: List<Map<List<List<Int>>, List<String>>>):
      List<List<String>> {
                return mock(
                        methodName = Method.complexFoo,
        arguments = mapOf(Arg.fooArg to fooArg)
                    )
  }

  public override fun foo(fooArg: List<List<Int>>): List<List<String>> {
                return mock(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg)
                    )
  }

  public override fun foo(fooArg: Map<Int, String>): Map<String, Int> {
                return mock(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg)
                    )
  }

  public object Method {
    public const val complexFoo: String = "complexFoo"

    public const val foo: String = "foo"
  }

  public object Arg {
    public const val fooArg: String = "fooArg"
  }

  public object Property {
    public const val getG1: String = "getG1"

    public const val setG1: String = "setG1"

    public const val getG2: String = "getG2"

    public const val setG2: String = "setG2"

    public const val `value`: String = "value"
  }
}
