package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.Map

public class PippoSampleMock : PippoSample, Mock {
  public override val uuid: String by uuid()

  public override var currentMutableSession: Int
    get() = mock(
        methodName = Property.getCurrentMutableSession
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setCurrentMutableSession,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override val currentSession: Int
    get() = mock(
        methodName = Property.getCurrentSession
    )

  public override fun sayHi() = mockUnit(
      methodName = Method.sayHi
  )

  public override fun sayHiWith(`param`: String): Unit {
                return mockUnit(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param)
                    )
  }

  public override fun sayHiWith(`param`: String, someOtherParam: Boolean): Unit {
                return mockUnit(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.someOtherParam to someOtherParam)
                    )
  }

  public override fun sayHiWith(`param`: String, entry: Map.Entry<String, String>): Unit {
                return mockUnit(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.entry to entry)
                    )
  }

  public override fun sayHiWith(`param`: String, map: Map<String, String>): Unit {
                return mockUnit(
                        methodName = Method.sayHiWith,
        arguments = mapOf(Arg.`param` to param,Arg.map to map)
                    )
  }

  public override fun sayHiWithCommonParam(`param`: String, intParam: Int): Unit {
                return mockUnit(
                        methodName = Method.sayHiWithCommonParam,
        arguments = mapOf(Arg.`param` to param,Arg.intParam to intParam)
                    )
  }

  public override fun showRandom(): Boolean = mock(
      methodName = Method.showRandom
  )

  public override suspend fun thisIsSuspend(`param`: String, intParam: Int): Unit {
                return mockUnit(
                        methodName = Method.thisIsSuspend,
        arguments = mapOf(Arg.`param` to param,Arg.intParam to intParam)
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
