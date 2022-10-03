package com.careem.mockingbird.kspsample

import com.careem.mockingbird.test.Spy
import com.careem.mockingbird.test.spy
import com.careem.mockingbird.test.uuid
import kotlin.String
import kotlin.Unit
import kotlin.reflect.KClass

public class UiDelegate2Args_UiState_ValueSpy(
  public val uiDelegate2Args: UiDelegate2Args<UiState, Value>,
) : UiDelegate2Args<UiState, Value>, Spy {
  public override val uuid: String by uuid()

  public override var s: UiState
    get() {
                  return spy(
                          methodName = Property.getS,
          delegate = { uiDelegate2Args.s }
                      )
    }
    set(`value`) {
                  return spy(
                          methodName = Property.setS,
          arguments = mapOf(Property.`value` to value),
          delegate = { uiDelegate2Args.s = value }
                      )
    }

  public override val t: Value
    get() {
                  return spy(
                          methodName = Property.getT,
          delegate = { uiDelegate2Args.t }
                      )
    }

  public override fun present(uiState: UiState): Unit {
                return spy(
                        methodName = Method.present,
        arguments = mapOf(Arg.uiState to uiState),
        delegate = { uiDelegate2Args.present(uiState) }
                    )
  }

  public override fun present(uiState: Value): Unit {
                return spy(
                        methodName = Method.present,
        arguments = mapOf(Arg.uiState to uiState),
        delegate = { uiDelegate2Args.present(uiState) }
                    )
  }

  public override fun remove(uiStateType: KClass<out UiState>): Unit {
                return spy(
                        methodName = Method.remove,
        arguments = mapOf(Arg.uiStateType to uiStateType),
        delegate = { uiDelegate2Args.remove(uiStateType) }
                    )
  }

  public override fun ret(): UiState {
                return spy(
                        methodName = Method.ret,
        delegate = { uiDelegate2Args.ret() }
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
