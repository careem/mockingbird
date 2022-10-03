package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.Double
import kotlin.Function0
import kotlin.Function1
import kotlin.Function2
import kotlin.Function3
import kotlin.Function4
import kotlin.Function5
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map

public class LambdaSampleSpy(
  public val lambdaSample: LambdaSample,
) : LambdaSample, Spy {
  public override val uuid: String by uuid()

  public override fun lambda00(`param`: String, lambda: Function0<Int>): Unit {
                return spy(
                        methodName = Method.lambda00,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda00(param,lambda) }
                    )
  }

  public override fun lambda01(`param`: String, lambda: Function0<Unit>): Unit {
                return spy(
                        methodName = Method.lambda01,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda01(param,lambda) }
                    )
  }

  public override fun lambda10(`param`: String, lambda: Function1<Int, Unit>): Unit {
                return spy(
                        methodName = Method.lambda10,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda10(param,lambda) }
                    )
  }

  public override fun lambda11(`param`: String, lambda: Function1<Int, Double>): Unit {
                return spy(
                        methodName = Method.lambda11,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda11(param,lambda) }
                    )
  }

  public override fun lambda2(`param`: String, lambda: Function2<String, Int, Unit>): Unit {
                return spy(
                        methodName = Method.lambda2,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda2(param,lambda) }
                    )
  }

  public override fun lambda3(`param`: String,
      lambda: Function3<String, Int, Map<String, Int>, Unit>): Unit {
                return spy(
                        methodName = Method.lambda3,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda3(param,lambda) }
                    )
  }

  public override fun lambda4(`param`: String,
      lambda: Function4<Boolean, Int, String, Double, Unit>): Unit {
                return spy(
                        methodName = Method.lambda4,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda4(param,lambda) }
                    )
  }

  public override fun lambda5(`param`: String,
      lambda: Function5<Boolean, Int, String, Double, List<String>, Unit>): Unit {
                return spy(
                        methodName = Method.lambda5,
        arguments = mapOf(Arg.`param` to param,Arg.lambda to lambda),
        delegate = { lambdaSample.lambda5(param,lambda) }
                    )
  }

  public object Method {
    public const val lambda00: String = "lambda00"

    public const val lambda01: String = "lambda01"

    public const val lambda10: String = "lambda10"

    public const val lambda11: String = "lambda11"

    public const val lambda2: String = "lambda2"

    public const val lambda3: String = "lambda3"

    public const val lambda4: String = "lambda4"

    public const val lambda5: String = "lambda5"
  }

  public object Arg {
    public const val `param`: String = "param"

    public const val lambda: String = "lambda"
  }

  public object Property
}
