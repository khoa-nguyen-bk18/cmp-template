package com.devindie.vaulty.domain.model.index

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.secondsFromGMT
import platform.Foundation.timeIntervalSince1970

private const val MS_PER_SECOND = 1000.0
private const val SECONDS_PER_MINUTE = 60

actual fun vaultSearchDslClock(): VaultSearchDslClock = object : VaultSearchDslClock {
    override fun nowEpochMs(): Long = (NSDate().timeIntervalSince1970 * MS_PER_SECOND).toLong()

    override fun zoneOffsetMinutes(): Int =
        (NSCalendar.currentCalendar.timeZone.secondsFromGMT / SECONDS_PER_MINUTE).toInt()
}
