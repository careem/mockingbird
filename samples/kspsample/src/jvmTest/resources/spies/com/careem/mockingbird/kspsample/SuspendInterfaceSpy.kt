package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.suspendSpy
import com.careem.mockingbird.test.uuid
import kotlin.String

public class SuspendInterfaceSpy(
  public val suspendInterface: SuspendInterface,
) : SuspendInterface, Spy {
  public override val uuid: String by uuid()

  public override suspend fun suspendFunction(a: String): String {
                return suspendSpy(
                        methodName = Method.suspendFunction,
        arguments = mapOf(Arg.a to a),
        delegate = { suspendInterface.suspendFunction(a) }
                    )
  }

  public object Method {
    public const val suspendFunction: String = "suspendFunction"
  }

  public object Arg {
    public const val a: String = "a"
  }

  public object Property
}
