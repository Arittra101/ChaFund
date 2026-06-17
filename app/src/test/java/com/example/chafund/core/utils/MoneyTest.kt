package com.example.chafund.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test fun zeroFormatsCorrectly() =
        assertEquals("Tk 0.00", Money.Zero.formatTk())

    @Test fun positiveFormatsWithGrouping() =
        assertEquals("Tk 1,250.50", Money(125050).formatTk())

    @Test fun negativeFormatsWithMinusSign() =
        assertEquals("Tk -1,250.50", Money(-125050).formatTk())

    @Test fun subTakaFormatsTwoDecimals() =
        assertEquals("Tk 0.99", Money(99).formatTk())

    @Test fun largeAmountGrouping() =
        assertEquals("Tk 1,000,000.00", Money(100_000_000L).formatTk())

    @Test fun fromTkRoundTrip() =
        assertEquals(Money(125050), Money.fromTk(1250.50))

    @Test fun fromTkRoundsHalfUp() =
        assertEquals(Money(1235), Money.fromTk(12.345))

    @Test fun fromTkStringValid() =
        assertEquals(Money(10000), Money.fromTkString("100"))

    @Test fun fromTkStringInvalidReturnsNull() =
        assertNull(Money.fromTkString("abc"))

    @Test fun fromTkStringEmptyReturnsNull() =
        assertNull(Money.fromTkString(""))

    @Test fun addition() =
        assertEquals(Money(300), Money(100) + Money(200))

    @Test fun subtraction() =
        assertEquals(Money(100), Money(300) - Money(200))

    @Test fun unaryMinus() =
        assertEquals(Money(-500), -Money(500))

    @Test fun isNegativeFlag() {
        assertTrue(Money(-1).isNegative)
        assertTrue(!Money(0).isNegative)
        assertTrue(!Money(1).isNegative)
    }
}
