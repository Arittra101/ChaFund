package com.example.chafund.core.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeFormat {

    private val DATE_DISPLAY  = DateTimeFormatter.ofPattern("dd MMMM yy",  Locale.ENGLISH)
    private val DATE_SHORT    = DateTimeFormatter.ofPattern("dd MMM yy",   Locale.ENGLISH)
    private val DAY_SHORT     = DateTimeFormatter.ofPattern("EEE",          Locale.ENGLISH)
    private val DAY_NAME      = DateTimeFormatter.ofPattern("EEEE",         Locale.ENGLISH)
    private val TIME_DISPLAY  = DateTimeFormatter.ofPattern("HH:mm",        Locale.ENGLISH)
    private val MONTH_LABEL   = DateTimeFormatter.ofPattern("MMMM yyyy",    Locale.ENGLISH)

    /** "16 June 26" */
    fun formatDate(date: LocalDate): String = date.format(DATE_DISPLAY)
    fun formatDate(epochDay: Long): String  = formatDate(LocalDate.ofEpochDay(epochDay))

    /** "16 Jun 26" – compact hint format */
    fun formatDateShort(date: LocalDate): String = date.format(DATE_SHORT)
    fun formatDateShort(epochDay: Long): String  = formatDateShort(LocalDate.ofEpochDay(epochDay))

    /** "Tue" */
    fun dayNameShort(date: LocalDate): String = date.format(DAY_SHORT)

    /** "Tuesday" */
    fun dayName(date: LocalDate): String  = date.format(DAY_NAME)
    fun dayName(epochDay: Long): String   = dayName(LocalDate.ofEpochDay(epochDay))

    /** "14:30" */
    fun formatTime(time: LocalTime): String = time.format(TIME_DISPLAY)

    /** "June 2026" */
    fun monthLabel(year: Int, month: Int): String =
        LocalDate.of(year, month, 1).format(MONTH_LABEL)

    /** Current epoch-day */
    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    /** Current time as "HH:mm" */
    fun nowTime(): String = formatTime(LocalTime.now())
}
