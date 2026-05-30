package com.devindie.vaulty.domain.model.index

/**
 * Resolves calendar vocabulary for `modified:` / `created:` DSL values to epoch ranges.
 */
internal object VaultSearchDslDateRanges {
    private const val DAY_MS = 86_400_000L
    private const val MS_PER_MINUTE = 60_000L
    private const val ISO_DATE_PART_COUNT = 3
    private const val MONTH_MIN = 1
    private const val MONTH_MAX = 12
    private const val DAY_MIN = 1
    private const val DAY_MAX = 31
    private const val WEEK_ANCHOR_OFFSET = 4
    private const val DAYS_PER_WEEK = 7
    private const val LAST_SEVEN_DAYS_OFFSET = -6
    private const val EPOCH_YEAR_START = 1970
    private const val DAYS_IN_LEAP_YEAR = 366
    private const val DAYS_IN_COMMON_YEAR = 365
    private const val JANUARY = 1
    private const val DECEMBER = 12
    private const val FIRST_DAY_OF_MONTH = 1
    private const val FEBRUARY = 2
    private const val APRIL = 4
    private const val JUNE = 6
    private const val SEPTEMBER = 9
    private const val NOVEMBER = 11
    private const val DAYS_IN_SHORT_MONTH = 30
    private const val DAYS_IN_LONG_MONTH = 31
    private const val LEAP_YEAR_DIVISOR = 4
    private const val CENTURY_DIVISOR = 100
    private const val QUADRICENTENNIAL_DIVISOR = 400
    private const val FEBRUARY_LEAP_DAYS = 29
    private const val FEBRUARY_COMMON_DAYS = 28

    data class EpochRange(val fromEpochMs: Long, val toEpochMs: Long)

    fun resolve(value: String, clock: VaultSearchDslClock): EpochRange? {
        val normalized = value.trim().lowercase()
        return resolveIsoDate(normalized, clock)
            ?: resolveRelativeKeyword(normalized, clock)
    }

    private fun resolveIsoDate(normalized: String, clock: VaultSearchDslClock): EpochRange? {
        if (!ISO_DATE_REGEX.matches(normalized)) return null
        val parts = normalized.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull()
        val month = parts.getOrNull(1)?.toIntOrNull()
        val day = parts.getOrNull(2)?.toIntOrNull()
        return year?.let { y ->
            month?.let { m ->
                day?.let { d ->
                    when {
                        m !in MONTH_MIN..MONTH_MAX || d !in DAY_MIN..DAY_MAX -> null
                        else -> {
                            val start = epochMsForLocalDayIndex(clock, epochDayIndex(y, m, d))
                            EpochRange(start, start + DAY_MS)
                        }
                    }
                }
            }
        }
    }

    private fun resolveRelativeKeyword(normalized: String, clock: VaultSearchDslClock): EpochRange? {
        val todayStart = startOfLocalDay(clock, dayOffsetFromToday = 0)
        val dayIndex = localDayIndex(clock)
        val weekStartDayIndex = dayIndex - ((dayIndex + WEEK_ANCHOR_OFFSET) % DAYS_PER_WEEK)
        val ymd = localYmd(clock)
        val (prevYear, prevMonth) =
            if (ymd.month == JANUARY) {
                ymd.year - 1 to DECEMBER
            } else {
                ymd.year to ymd.month - 1
            }
        return when (normalized) {
            "today" -> EpochRange(todayStart, todayStart + DAY_MS)
            "yesterday" -> {
                val start = startOfLocalDay(clock, dayOffsetFromToday = -1)
                EpochRange(start, todayStart)
            }
            "last_7_days", "last7days" -> {
                val start = startOfLocalDay(clock, dayOffsetFromToday = LAST_SEVEN_DAYS_OFFSET)
                EpochRange(start, todayStart + DAY_MS)
            }
            "this_week" ->
                EpochRange(
                    epochMsForLocalDayIndex(clock, weekStartDayIndex),
                    todayStart + DAY_MS,
                )
            "last_week" -> {
                val lastWeekStart = weekStartDayIndex - DAYS_PER_WEEK
                EpochRange(
                    epochMsForLocalDayIndex(clock, lastWeekStart),
                    epochMsForLocalDayIndex(clock, weekStartDayIndex),
                )
            }
            "this_month" ->
                EpochRange(
                    monthStartEpoch(clock, ymd.year, ymd.month),
                    todayStart + DAY_MS,
                )
            "last_month" ->
                EpochRange(
                    monthStartEpoch(clock, prevYear, prevMonth),
                    monthStartEpoch(clock, ymd.year, ymd.month),
                )
            else -> null
        }
    }

    private fun startOfLocalDay(clock: VaultSearchDslClock, dayOffsetFromToday: Int): Long =
        epochMsForLocalDayIndex(clock, localDayIndex(clock) + dayOffsetFromToday)

    private fun localDayIndex(clock: VaultSearchDslClock): Long {
        val offsetMs = clock.zoneOffsetMinutes().toLong() * MS_PER_MINUTE
        return (clock.nowEpochMs() + offsetMs) / DAY_MS
    }

    private fun epochMsForLocalDayIndex(clock: VaultSearchDslClock, dayIndex: Long): Long {
        val offsetMs = clock.zoneOffsetMinutes().toLong() * MS_PER_MINUTE
        return dayIndex * DAY_MS - offsetMs
    }

    private data class Ymd(val year: Int, val month: Int, val day: Int)

    private fun localYmd(clock: VaultSearchDslClock): Ymd {
        var remaining = localDayIndex(clock)
        var year = EPOCH_YEAR_START
        while (true) {
            val daysInYear = if (isLeapYear(year)) DAYS_IN_LEAP_YEAR else DAYS_IN_COMMON_YEAR
            if (remaining < daysInYear) break
            remaining -= daysInYear
            year++
        }
        var month = MONTH_MIN
        while (month <= MONTH_MAX) {
            val dim = daysInMonth(year, month)
            if (remaining < dim) break
            remaining -= dim
            month++
        }
        return Ymd(year, month, (remaining + 1).toInt())
    }

    private fun monthStartEpoch(clock: VaultSearchDslClock, year: Int, month: Int): Long =
        epochMsForLocalDayIndex(clock, epochDayIndex(year, month, FIRST_DAY_OF_MONTH))

    private fun epochDayIndex(year: Int, month: Int, day: Int): Long {
        var days = 0L
        for (y in EPOCH_YEAR_START until year) {
            days += if (isLeapYear(y)) DAYS_IN_LEAP_YEAR else DAYS_IN_COMMON_YEAR
        }
        for (m in MONTH_MIN until month) {
            days += daysInMonth(year, m)
        }
        days += (day - FIRST_DAY_OF_MONTH)
        return days
    }

    private fun isLeapYear(year: Int): Boolean = (year % LEAP_YEAR_DIVISOR == 0 && year % CENTURY_DIVISOR != 0) ||
        year % QUADRICENTENNIAL_DIVISOR == 0

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        FEBRUARY -> if (isLeapYear(year)) FEBRUARY_LEAP_DAYS else FEBRUARY_COMMON_DAYS
        APRIL, JUNE, SEPTEMBER, NOVEMBER -> DAYS_IN_SHORT_MONTH
        else -> DAYS_IN_LONG_MONTH
    }

    private val ISO_DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")
}
