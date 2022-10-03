package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.String
import kotlin.Unit

public class Mock1Spy(
  public val mock1: Mock1,
) : Mock1, Spy {
  public override val uuid: String by uuid()

  public override var nullableProperty: String?
    get() {
                  return spy(
                          methodName = Property.getNullableProperty,
          delegate = { mock1.nullableProperty }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setNullableProperty,
          arguments = mapOf(Property.`value` to value),
          delegate = { mock1.nullableProperty = value }
                      )
    }

  public override fun foo(): Boolean {
                return spy(
                        methodName = Method.foo,
        delegate = { mock1.foo() }
                    )
  }

  public override fun foo1(): Unit {
                return spy(
                        methodName = Method.foo1,
        delegate = { mock1.foo1() }
                    )
  }

  public override fun foo2(string: String): Unit {
                return spy(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.string to string),
        delegate = { mock1.foo2(string) }
                    )
  }

  public override fun foo3(string2: String, someOtherParam: Boolean): Unit {
                return spy(
                        methodName = Method.foo3,
        arguments = mapOf(Arg.string2 to string2,Arg.someOtherParam to someOtherParam),
        delegate = { mock1.foo3(string2,someOtherParam) }
                    )
  }

  public override fun nullableFoo(nullString: String?): String? {
                return spy(
                        methodName = Method.nullableFoo,
        arguments = mapOf(Arg.nullString to nullString),
        delegate = { mock1.nullableFoo(nullString) }
                    )
  }

  public object Method {
    public const val foo: String = "foo"

    public const val foo1: String = "foo1"

    public const val foo2: String = "foo2"

    public const val foo3: String = "foo3"

    public const val nullableFoo: String = "nullableFoo"
  }

  public object Arg {
    public const val string: String = "string"

    public const val string2: String = "string2"

    public const val someOtherParam: String = "someOtherParam"

    public const val nullString: String = "nullString"
  }

  public object Property {
    public const val getNullableProperty: String = "getNullableProperty"

    public const val setNullableProperty: String = "setNullableProperty"

    public const val `value`: String = "value"
  }
}
