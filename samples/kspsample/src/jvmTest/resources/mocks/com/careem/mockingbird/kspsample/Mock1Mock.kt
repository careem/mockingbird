package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.String
import kotlin.Unit

public class Mock1Mock : Mock1, Mock {
  public override val uuid: String by uuid()

  public override var nullableProperty: String?
    get() = mock(
        methodName = Property.getNullableProperty
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setNullableProperty,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override fun foo(): Boolean = mock(
      methodName = Method.foo
  )

  public override fun foo1() = mockUnit(
      methodName = Method.foo1
  )

  public override fun foo2(string: String): Unit {
                return mockUnit(
                        methodName = Method.foo2,
        arguments = mapOf(Arg.string to string)
                    )
  }

  public override fun foo3(string2: String, someOtherParam: Boolean): Unit {
                return mockUnit(
                        methodName = Method.foo3,
        arguments = mapOf(Arg.string2 to string2,Arg.someOtherParam to someOtherParam)
                    )
  }

  public override fun nullableFoo(nullString: String?): String? {
                return mock(
                        methodName = Method.nullableFoo,
        arguments = mapOf(Arg.nullString to nullString)
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
