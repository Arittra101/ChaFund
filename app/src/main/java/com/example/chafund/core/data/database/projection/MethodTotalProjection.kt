package com.example.chafund.core.data.database.projection

/** Sum of amounts for one payment method; [paymentMethodId] is null for pre-v6 rows. */
data class MethodTotalProjection(
    val paymentMethodId: Long?,
    val totalPaisa: Long,
)
