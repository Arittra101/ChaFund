package com.example.chafund.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DateTimeFormatTest {

    @Test fun formatDateReturnsDdMmmmYy() =
        assertEquals("16 June 26", DateTimeFormat.formatDate(LocalDate.of(2026, 6, 16)))

    @Test fun formatDateFromEpochDay() =
        assertEquals("16 June 26", DateTimeFormat.formatDate(LocalDate.of(2026, 6, 16).toEpochDay()))

    @Test fun dayNameReturnsFullDay() =
        assertEquals("Tuesday", DateTimeFormat.dayName(LocalDate.of(2026, 6, 16)))

    @Test fun dayNameFromEpochDay() =
        assertEquals("Tuesday", DateTimeFormat.dayName(LocalDate.of(2026, 6, 16).toEpochDay()))

    @Test fun formatTimeReturnsHhMm() =
        assertEquals("14:30", DateTimeFormat.formatTime(LocalTime.of(14, 30)))

    @Test fun formatTimePadsSingleDigitHour() =
        assertEquals("09:05", DateTimeFormat.formatTime(LocalTime.of(9, 5)))

    @Test fun monthLabelReturnsMmmmYyyy() =
        assertEquals("June 2026", DateTimeFormat.monthLabel(2026, 6))

    @Test fun monthLabelJanuary() =
        assertEquals("January 2026", DateTimeFormat.monthLabel(2026, 1))
}
