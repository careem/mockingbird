package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit

internal class InternalSampleInterfaceSpy(
  public val internalSampleInterface: InternalSampleInterface,
) : InternalSampleInterface, Spy {
  public override val uuid: String by uuid()

  public override fun thisIsInternal(`param`: String): Unit {
                return spy(
                        methodName = Method.thisIsInternal,
        arguments = mapOf(Arg.`param` to param),
        delegate = { internalSampleInterface.thisIsInternal(param) }
                    )
  }

  public object Method {
    public const val thisIsInternal: String = "thisIsInternal"
  }

  public object Arg {
    public const val `param`: String = "param"
  }

  public object Property
}
