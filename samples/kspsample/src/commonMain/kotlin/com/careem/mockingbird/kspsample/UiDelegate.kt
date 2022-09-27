package com.careem.mockingbird.kspsample

import kotlin.reflect.KClass

public interface UiState

public interface UiDelegate<S : UiState> {
    var s: S
    val t: S

    public fun present(uiState: S)
    public fun remove(uiStateType: KClass<out UiState>)
    public fun ret(): S
}