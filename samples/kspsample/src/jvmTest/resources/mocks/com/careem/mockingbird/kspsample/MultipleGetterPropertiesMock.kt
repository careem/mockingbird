package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.String

public class MultipleGetterPropertiesMock : MultipleGetterProperties, Mock {
  public override val uuid: String by uuid()

  public override val isA: Boolean
    get() = mock(
        methodName = Property.isA
    )

  public override val isB: Boolean
    get() = mock(
        methodName = Property.isB
    )

  public object Method

  public object Arg

  public object Property {
    public const val isA: String = "isA"

    public const val isB: String = "isB"
  }
}
