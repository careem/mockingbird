package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public class InterfaceWithGenericsSpy(
  public val interfaceWithGenerics: InterfaceWithGenerics,
) : InterfaceWithGenerics, Spy {
  public override val uuid: String by uuid()

  public override var g1: List<String>
    get() {
                  return spy(
                          methodName = Property.getG1,
          delegate = { interfaceWithGenerics.g1 }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setG1,
          arguments = mapOf(Property.`value` to value),
          delegate = { interfaceWithGenerics.g1 = value }
                      )
    }

  public override var g2: Map<String, Int>
    get() {
                  return spy(
                          methodName = Property.getG2,
          delegate = { interfaceWithGenerics.g2 }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setG2,
          arguments = mapOf(Property.`value` to value),
          delegate = { interfaceWithGenerics.g2 = value }
                      )
    }

  public override fun complexFoo(fooArg: List<Map<List<List<Int>>, List<String>>>):
      List<List<String>> {
                return spy(
                        methodName = Method.complexFoo,
        arguments = mapOf(Arg.fooArg to fooArg),
        delegate = { interfaceWithGenerics.complexFoo(fooArg) }
                    )
  }

  public override fun foo(fooArg: List<List<Int>>): List<List<String>> {
                return spy(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg),
        delegate = { interfaceWithGenerics.foo(fooArg) }
                    )
  }

  public override fun foo(fooArg: Map<Int, String>): Map<String, Int> {
                return spy(
                        methodName = Method.foo,
        arguments = mapOf(Arg.fooArg to fooArg),
        delegate = { interfaceWithGenerics.foo(fooArg) }
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
