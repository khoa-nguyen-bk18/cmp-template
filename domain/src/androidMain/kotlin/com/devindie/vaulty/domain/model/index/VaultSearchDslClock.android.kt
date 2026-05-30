package com.devindie.vaulty.domain.model.index

import java.util.TimeZone

private const val MS_PER_MINUTE = 60_000

actual fun vaultSearchDslClock(): VaultSearchDslClock = object : VaultSearchDslClock {
    override fun nowEpochMs(): Long = System.currentTimeMillis()

    override fun zoneOffsetMinutes(): Int = TimeZone.getDefault().getOffset(nowEpochMs()) / MS_PER_MINUTE
}
