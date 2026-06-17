package com.example.chafund.core.domain

sealed interface Result<out D, out E : RootError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

inline fun <D, E : RootError, R> Result<D, E>.map(
    transform: (D) -> R,
): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
}

inline fun <D, E : RootError> Result<D, E>.onSuccess(
    action: (D) -> Unit,
): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : RootError> Result<D, E>.onError(
    action: (E) -> Unit,
): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}

inline fun <D, E : RootError, R> Result<D, E>.fold(
    onSuccess: (D) -> R,
    onError: (E) -> R,
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error   -> onError(error)
}
