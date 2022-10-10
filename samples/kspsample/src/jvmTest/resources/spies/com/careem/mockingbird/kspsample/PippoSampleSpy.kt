package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.suspendSpy
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.Map

public class PippoSampleSpy(
  public val pippoSample: PippoSample,
) : PippoSample, Spy {
  public override val uuid: String by uuid()

  public override var currentMutableSession: Int
    get() {
                  return spy(
                          methodName = Property.getCurrentMutableSession,
          delegate = { pippoSample.currentMutableSession }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setCurrentMutableSession,
          arguments = mapOf(Property.`value` to value),
          delegate = { pippoSample.currentMutableSession = value }
                      )
    }

  public override val currentSession: Int
    get() {
                  return spy(
                          methodName = Property.getCurrentSession,
          delegate = { pippoSample.currentSession }
                      )
    }

  public override fun sayHi(): Unit {
                return spy(
                        methodName = Method.sayHi,
        delegate = { pippoSample.sayHi() }
                    )
  }

  public override fun sayHiWith(`param`: String): Unit {
                return spy(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param),
        delegate = { pippoSample.sayHiWith(param) }
                    )
  }

  public override fun sayHiWith(`param`: String, someOtherParam: Boolean): Unit {
                return spy(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.someOtherParam to someOtherParam),
        delegate = { pippoSample.sayHiWith(param,someOtherParam) }
                    )
  }

  public override fun sayHiWith(`param`: String, entry: Map.Entry<String, String>): Unit {
                return spy(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.entry to entry),
        delegate = { pippoSample.sayHiWith(param,entry) }
                    )
  }

  public override fun sayHiWith(`param`: String, map: Map<String, String>): Unit {
                return spy(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.map to map),
        delegate = { pippoSample.sayHiWith(param,map) }
                    )
  }

  public override fun sayHiWithCommonParam(`param`: String, intParam: Int): Unit {
                return spy(
                        methodName = Method.sayHiWithCommonParam,
        arguments = mapOf(Arg.`param` to param,Arg.intParam to intParam),
        delegate = { pippoSample.sayHiWithCommonParam(param,intParam) }
                    )
  }

  public override fun showRandom(): Boolean {
                return spy(
                        methodName = Method.showRandom,
        delegate = { pippoSample.showRandom() }
                    )
  }

  public override suspend fun thisIsSuspend(`param`: String, intParam: Int): Unit {
                return suspendSpy(
                        methodName = Method.thisIsSuspend,
        arguments = mapOf(Arg.`param` to param,Arg.intParam to intParam),
        delegate = { pippoSample.thisIsSuspend(param,intParam) }
                    )
  }

  public object Method {
    public const val sayHi: String = "sayHi"

    public const val sayHiWith: String = "sayHiWith"

    public const val sayHiWithCommonParam: String = "sayHiWithCommonParam"

    public const val showRandom: String = "showRandom"

    public const val thisIsSuspend: String = "thisIsSuspend"
  }

  public object Arg {
    public const val `param`: String = "param"

    public const val someOtherParam: String = "someOtherParam"

    public const val entry: String = "entry"

    public const val map: String = "map"

    public const val intParam: String = "intParam"
  }

  public object Property {
    public const val getCurrentMutableSession: String = "getCurrentMutableSession"

    public const val setCurrentMutableSession: String = "setCurrentMutableSession"

    public const val getCurrentSession: String = "getCurrentSession"

    public const val `value`: String = "value"
  }
}
