package com.careem.mockingbird.util

import kotlin.js.Date

actual fun getSystemTimeInMillis(): Long = Date.now().toLong()