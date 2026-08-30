package com.example.chafund.core.utils

import java.time.LocalDate

/**
 * Helpers for a month's optional "cycle" window that reaches back into the previous
 * calendar month. See the custom-cycle feature: a month can include a tail of the
 * previous month starting at [cycleStartEpochDay].
 */
object MonthWindow {

    /** Epoch-day of the first day of the given calendar month. */
    fun firstOfMonthEpochDay(year: Int, month: Int): Long =
        LocalDate.of(year, month, 1).toEpochDay()

    /** Last day of the previous calendar month (the inclusive end of any tail). */
    fun tailEndFor(year: Int, month: Int): Long =
        firstOfMonthEpochDay(year, month) - 1

    /**
     * The active tail start for a month, or null when no tail should be counted
     * (no cycle set, or the include toggle is off).
     */
    fun tailStart(cycleStartEpochDay: Long?, includePrevTail: Boolean): Long? =
        if (cycleStartEpochDay != null && includePrevTail) cycleStartEpochDay else null
}
