package com.careem.mockingbird.util

import platform.Foundation.NSDate
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970

internal actual fun getSystemTimeInMillis() = (NSDate.date().timeIntervalSince1970 * 1000.0).toLong()
