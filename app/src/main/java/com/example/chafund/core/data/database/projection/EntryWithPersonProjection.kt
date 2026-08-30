package com.example.chafund.core.data.database.projection

import androidx.room.Embedded
import com.example.chafund.core.data.database.entity.EntryEntity

data class EntryWithPersonProjection(
    @Embedded val entry: EntryEntity,
    val personName: String?,
    val groupName: String?,
)
