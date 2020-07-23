package com.careem.mockingbird.test

import platform.Foundation.NSThread

actual fun sleep(millis: Long) = NSThread.sleepForTimeInterval(millis.toFloat() / 1000.0)