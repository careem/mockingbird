package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.String

public class MultipleGetterPropertiesSpy(
  public val multipleGetterProperties: MultipleGetterProperties,
) : MultipleGetterProperties, Spy {
  public override val uuid: String by uuid()

  public override val isA: Boolean
    get() {
                  return spy(
                          methodName = Property.isA,
          delegate = { multipleGetterProperties.isA }
                      )
    }

  public override val isB: Boolean
    get() {
                  return spy(
                          methodName = Property.isB,
          delegate = { multipleGetterProperties.isB }
                      )
    }

  public object Method

  public object Arg

  public object Property {
    public const val isA: String = "isA"

    public const val isB: String = "isB"
  }
}
