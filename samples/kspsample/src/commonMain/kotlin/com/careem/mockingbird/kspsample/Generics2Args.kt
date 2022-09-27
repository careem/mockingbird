package com.careem.mockingbird.kspsample

import kotlin.reflect.KClass

public interface Value

public interface UiDelegate2Args<S : UiState, T: Value> {
    var s: S
    val t: T

    public fun present(uiState: S)
    public fun present(uiState: T)
    public fun remove(uiStateType: KClass<out UiState>)
    public fun ret(): S
}