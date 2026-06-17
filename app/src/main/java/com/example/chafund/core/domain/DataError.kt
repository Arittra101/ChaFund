package com.example.chafund.core.domain

sealed interface DataError : RootError {
    enum class Local : DataError {
        NOT_FOUND,
        DISK_FULL,
        UNKNOWN,
    }
}
