package com.example.chafund.feature.fund.data.mapper

import com.example.chafund.core.data.database.entity.EntryEntity
import com.example.chafund.core.data.database.entity.ExpenseEntity
import com.example.chafund.core.data.database.entity.MonthEntity
import com.example.chafund.core.data.database.entity.PersonGroupEntity
import com.example.chafund.core.data.database.entity.TimeCategoryEntity
import com.example.chafund.core.data.database.projection.PersonWithGroupProjection
import com.example.chafund.core.utils.Money
import com.example.chafund.feature.fund.domain.model.Group
import com.example.chafund.feature.fund.domain.model.Month
import com.example.chafund.feature.fund.domain.model.Person
import com.example.chafund.feature.fund.domain.model.TimeCategory

fun MonthEntity.toDomain() = Month(
    id        = id,
    year      = year,
    month     = month,
    label     = label,
    isCurrent = isCurrent,
    cycleStartEpochDay = cycleStartEpochDay,
    includePrevTail    = includePrevTail,
)

fun TimeCategoryEntity.toDomain() = TimeCategory(
    id        = id,
    name      = name,
    sortOrder = sortOrder,
)

fun PersonGroupEntity.toDomain() = Group(
    id   = id,
    name = name,
)

fun PersonWithGroupProjection.toDomain() = Person(
    id        = id,
    name      = name,
    groupId   = groupId,
    groupName = groupName,
)
