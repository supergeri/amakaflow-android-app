package com.amakaflow.companion.debug

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class LogLevel(val indicator: String) {
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    SUCCESS("S")
}

enum class ErrorType(val displayName: String) {
    API_ERROR("API_ERROR"),
    WATCH_ERROR("WATCH_ERROR"),
    NETWORK_ERROR("NETWORK_ERROR"),
    AUTH_ERROR("AUTH_ERROR"),
    SYNC_ERROR("SYNC_ERROR"),
    APP_ERROR("APP_ERROR"),
    GENERAL("INFO")
}

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val level: LogLevel,
    val errorType: ErrorType = ErrorType.GENERAL,
    val title: String,
    val details: String? = null,
    val tag: String? = null,
    // API-specific fields
    val endpoint: String? = null,
    val method: String? = null,
    val response: String? = null,
    val statusCode: Int? = null
) {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun formattedTime(): String = timeFormat.format(timestamp)
    fun formattedDateTime(): String = dateTimeFormat.format(timestamp)

    fun formatted(): String {
        val levelIndicator = "[${level.indicator}]"
        val tagStr = tag?.let { "[$it] " } ?: ""
        return "${formattedTime()} $levelIndicator $tagStr$title"
    }

    fun copyableText(): String {
        val sb = StringBuilder()
        sb.appendLine("[${formattedDateTime()}] ${errorType.displayName}")
        sb.appendLine("Title: $title")
        details?.let { sb.appendLine("Details: $it") }
        endpoint?.let { sb.appendLine("Endpoint: $it") }
        method?.let { sb.appendLine("Method: $it") }
        response?.let { sb.appendLine("Response: $it") }
        statusCode?.let { sb.appendLine("Status: $it") }
        return sb.toString()
    }
}

/**
 * Global debug logging singleton that captures logs in-memory for on-device viewing.
 * Logs are also forwarded to Android's Logcat for debugging with Android Studio.
 */
object DebugLog {
    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries

    private const val MAX_ENTRIES = 500
    private const val DEFAULT_TAG = "AmakaFlow"

    private fun addEntry(entry: LogEntry) {
        synchronized(this) {
            _entries.value = (listOf(entry) + _entries.value).take(MAX_ENTRIES)
        }

        // Also log to Logcat for Android Studio debugging
        val logTag = entry.tag ?: DEFAULT_TAG
        val message = entry.details?.let { "${entry.title}: $it" } ?: entry.title
        when (entry.level) {
            LogLevel.DEBUG -> Log.d(logTag, message)
            LogLevel.INFO -> Log.i(logTag, message)
            LogLevel.WARNING -> Log.w(logTag, message)
            LogLevel.ERROR -> Log.e(logTag, message)
            LogLevel.SUCCESS -> Log.i(logTag, "OK: $message")
        }
    }

    // Simple logging methods (for general app events)
    fun log(message: String, level: LogLevel = LogLevel.INFO, tag: String? = null) {
        val entry = LogEntry(
            level = level,
            errorType = when (level) {
                LogLevel.ERROR -> ErrorType.APP_ERROR
                else -> ErrorType.GENERAL
            },
            title = message,
            tag = tag
        )
        addEntry(entry)
    }

    fun debug(message: String, tag: String? = null) = log(message, LogLevel.DEBUG, tag)
    fun info(message: String, tag: String? = null) = log(message, LogLevel.INFO, tag)
    fun warning(message: String, tag: String? = null) = log(message, LogLevel.WARNING, tag)
    fun error(message: String, tag: String? = null) = log(message, LogLevel.ERROR, tag)
    fun success(message: String, tag: String? = null) = log(message, LogLevel.SUCCESS, tag)

    // Structured error logging (matching iOS style)
    fun apiError(
        title: String,
        details: String? = null,
        endpoint: String? = null,
        method: String? = null,
        response: String? = null,
        statusCode: Int? = null,
        tag: String? = null
    ) {
        val entry = LogEntry(
            level = LogLevel.ERROR,
            errorType = ErrorType.API_ERROR,
            title = title,
            details = details,
            endpoint = endpoint,
            method = method,
            response = response,
            statusCode = statusCode,
            tag = tag
        )
        addEntry(entry)
    }

    fun watchError(
        title: String,
        details: String? = null,
        tag: String? = null
    ) {
        val entry = LogEntry(
            level = LogLevel.ERROR,
            errorType = ErrorType.WATCH_ERROR,
            title = title,
            details = details,
            tag = tag
        )
        addEntry(entry)
    }

    fun networkError(
        title: String,
        details: String? = null,
        tag: String? = null
    ) {
        val entry = LogEntry(
            level = LogLevel.ERROR,
            errorType = ErrorType.NETWORK_ERROR,
            title = title,
            details = details,
            tag = tag
        )
        addEntry(entry)
    }

    fun authError(
        title: String,
        details: String? = null,
        tag: String? = null
    ) {
        val entry = LogEntry(
            level = LogLevel.ERROR,
            errorType = ErrorType.AUTH_ERROR,
            title = title,
            details = details,
            tag = tag
        )
        addEntry(entry)
    }

    fun error(throwable: Throwable, tag: String? = null) {
        val stackTrace = throwable.stackTraceToString().take(500)
        val entry = LogEntry(
            level = LogLevel.ERROR,
            errorType = ErrorType.APP_ERROR,
            title = "${throwable.javaClass.simpleName}: ${throwable.message}",
            details = stackTrace,
            tag = tag
        )
        addEntry(entry)
    }

    fun clear() {
        synchronized(this) {
            _entries.value = emptyList()
        }
        info("Logs cleared", "DebugLog")
    }

    fun copyableText(): String {
        val sb = StringBuilder()
        sb.appendLine("=== AmakaFlow Debug Log ===")
        sb.appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
        sb.appendLine("Entries: ${_entries.value.size}")
        sb.appendLine()
        _entries.value.forEach { entry ->
            sb.appendLine(entry.copyableText())
        }
        return sb.toString()
    }

    val entryCount: Int
        get() = _entries.value.size
}
