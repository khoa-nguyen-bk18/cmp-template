package com.devindie.vaulty.domain.model.index

/**
 * Clock for resolving calendar DSL tokens (`modified:yesterday`, etc.) in a testable way.
 */
interface VaultSearchDslClock {
    fun nowEpochMs(): Long

    /** Local time zone offset from UTC in whole minutes (e.g. -420 for US Pacific). */
    fun zoneOffsetMinutes(): Int
}

/** Platform [VaultSearchDslClock] for production parsing. */
expect fun vaultSearchDslClock(): VaultSearchDslClock
