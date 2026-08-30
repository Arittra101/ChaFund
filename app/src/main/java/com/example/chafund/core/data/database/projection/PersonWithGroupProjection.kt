package com.example.chafund.core.data.database.projection

data class PersonWithGroupProjection(
    val id: Long,
    val name: String,
    val groupId: Long,
    val groupName: String,
)
