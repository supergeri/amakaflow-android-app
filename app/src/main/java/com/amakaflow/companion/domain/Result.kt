package com.amakaflow.companion.domain

/**
 * A sealed class representing the result of an operation.
 * Part of the domain layer - pure Kotlin with no Android dependencies.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Transform the data inside a Success result.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Perform an action when the result is Success.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Perform an action when the result is Error.
 */
inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) action(message)
    return this
}

/**
 * Get the data if Success, or null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Get the data if Success, or a default value otherwise.
 */
fun <T> Result<T>.getOrDefault(default: T): T = when (this) {
    is Result.Success -> data
    else -> default
}

/**
 * Returns true if the result is Success.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns true if the result is Error.
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Returns true if the result is Loading.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading
