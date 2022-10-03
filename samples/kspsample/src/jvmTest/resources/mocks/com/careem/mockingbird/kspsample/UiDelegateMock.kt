package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Mock
import com.careem.mockingbird.test.mock
import com.careem.mockingbird.test.mockUnit
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit
import kotlin.reflect.KClass

public class UiDelegate_UiStateMock : UiDelegate<UiState>, Mock {
  public override val uuid: String by uuid()

  public override var s: UiState
    get() = mock(
        methodName = Property.getS
    )
    set(`value`) {
                  return mockUnit(
                          methodName = Property.setS,
          arguments = mapOf(Property.`value` to value)
                      )
    }

  public override val t: UiState
    get() = mock(
        methodName = Property.getT
    )

  public override fun present(uiState: UiState): Unit {
                return mockUnit(
                        methodName = Method.present,
        arguments = mapOf(Arg.uiState to uiState)
                    )
  }

  public override fun remove(uiStateType: KClass<out UiState>): Unit {
                return mockUnit(
                        methodName = Method.remove,
        arguments = mapOf(Arg.uiStateType to uiStateType)
                    )
  }

  public override fun ret(): UiState = mock(
      methodName = Method.ret
  )

  public object Method {
    public const val present: String = "present"

    public const val remove: String = "remove"

    public const val ret: String = "ret"
  }

  public object Arg {
    public const val uiState: String = "uiState"

    public const val uiStateType: String = "uiStateType"
  }

  public object Property {
    public const val getS: String = "getS"

    public const val setS: String = "setS"

    public const val getT: String = "getT"

    public const val `value`: String = "value"
  }
}
