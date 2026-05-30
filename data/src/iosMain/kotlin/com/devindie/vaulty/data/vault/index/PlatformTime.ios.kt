package com.devindie.vaulty.data.vault.index

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

private const val MS_PER_SECOND = 1000

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentEpochMillis(): Long = (NSDate().timeIntervalSince1970 * MS_PER_SECOND).toLong()
