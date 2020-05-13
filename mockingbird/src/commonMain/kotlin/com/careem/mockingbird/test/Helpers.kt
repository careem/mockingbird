package com.careem.mockingbird.test

/**
 * Method to freeze state. Calls the platform implementation of 'freeze' on native, and is a noop on other platforms.
 */
expect fun <T> T.freeze(): T

/**
 * Determine if object is frozen. Will return false on non-native platforms.
 */
expect val <T> T.isFrozen: Boolean

/**
 * Call on an object which should never be frozen. Will help debug when something inadvertently is.
 */
expect fun Any.ensureNeverFrozen()