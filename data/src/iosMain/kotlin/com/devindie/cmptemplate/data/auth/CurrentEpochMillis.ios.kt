package com.devindie.cmptemplate.data.auth

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun currentEpochMillis(): Long = (NSDate().timeIntervalSince1970 * 1_000).toLong()
