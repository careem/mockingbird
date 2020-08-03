package com.careem.mockingbird.util

import kotlin.js.Date

internal actual fun getSystemTimeInMillis(): Long = Date.now().toLong()