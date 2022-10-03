package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit
import kotlin.reflect.KClass

public class UiDelegate_UiStateSpy(
  public val uiDelegate: UiDelegate<UiState>,
) : UiDelegate<UiState>, Spy {
  public override val uuid: String by uuid()

  public override var s: UiState
    get() {
                  return spy(
                          methodName = Property.getS,
          delegate = { uiDelegate.s }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setS,
          arguments = mapOf(Property.`value` to value),
          delegate = { uiDelegate.s = value }
                      )
    }

  public override val t: UiState
    get() {
                  return spy(
                          methodName = Property.getT,
          delegate = { uiDelegate.t }
                      )
    }

  public override fun present(uiState: UiState): Unit {
                return spy(
                        methodName = Method.present,
        arguments = mapOf(Arg.uiState to uiState),
        delegate = { uiDelegate.present(uiState) }
                    )
  }

  public override fun remove(uiStateType: KClass<out UiState>): Unit {
                return spy(
                        methodName = Method.remove,
        arguments = mapOf(Arg.uiStateType to uiStateType),
        delegate = { uiDelegate.remove(uiStateType) }
                    )
  }

  public override fun ret(): UiState {
                return spy(
                        methodName = Method.ret,
        delegate = { uiDelegate.ret() }
                    )
  }

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
