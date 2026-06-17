package com.example.chafund.core.utils

import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class Money(val paisa: Long) : Comparable<Money> {

    val isNegative: Boolean get() = paisa < 0
    val isZero: Boolean     get() = paisa == 0L
    val isPositive: Boolean get() = paisa > 0

    operator fun plus(other: Money)  = Money(paisa + other.paisa)
    operator fun minus(other: Money) = Money(paisa - other.paisa)
    operator fun unaryMinus()        = Money(-paisa)

    override fun compareTo(other: Money) = paisa.compareTo(other.paisa)

    /** "Tk 1,250.50" — negative produces "Tk -1,250.50" */
    fun formatTk(locale: Locale = Locale.ENGLISH): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val taka = paisa / 100.0
        return "Tk ${nf.format(taka)}"
    }

    companion object {
        val Zero = Money(0L)

        fun fromTk(tk: Double): Money = Money(Math.round(tk * 100.0))

        /** Returns null if text is not a valid positive number. */
        fun fromTkString(text: String): Money? =
            text.trim().toDoubleOrNull()?.let { fromTk(it) }
    }
}
