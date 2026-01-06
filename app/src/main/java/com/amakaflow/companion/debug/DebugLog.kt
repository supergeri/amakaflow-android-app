package com.amakaflow.companion.debug

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class LogLevel(val emoji: String) {
    DEBUG("D"),
    INFO("I"),
    WARNING("W"),
    ERROR("E"),
    SUCCESS("S")
}

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val level: LogLevel,
    val message: String,
    val tag: String? = null
) {
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun formatted(): String {
        val levelIndicator = "[${level.emoji}]"
        val tagStr = tag?.let { "[$it] " } ?: ""
        return "${dateFormat.format(timestamp)} $levelIndicator $tagStr$message"
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

    fun log(message: String, level: LogLevel = LogLevel.INFO, tag: String? = null) {
        val entry = LogEntry(level = level, message = message, tag = tag)
        synchronized(this) {
            _entries.value = (_entries.value + entry).takeLast(MAX_ENTRIES)
        }

        // Also log to Logcat for Android Studio debugging
        val logTag = tag ?: DEFAULT_TAG
        when (level) {
            LogLevel.DEBUG -> Log.d(logTag, message)
            LogLevel.INFO -> Log.i(logTag, message)
            LogLevel.WARNING -> Log.w(logTag, message)
            LogLevel.ERROR -> Log.e(logTag, message)
            LogLevel.SUCCESS -> Log.i(logTag, "OK: $message")
        }
    }

    fun debug(message: String, tag: String? = null) = log(message, LogLevel.DEBUG, tag)
    fun info(message: String, tag: String? = null) = log(message, LogLevel.INFO, tag)
    fun warning(message: String, tag: String? = null) = log(message, LogLevel.WARNING, tag)
    fun error(message: String, tag: String? = null) = log(message, LogLevel.ERROR, tag)
    fun success(message: String, tag: String? = null) = log(message, LogLevel.SUCCESS, tag)

    fun error(throwable: Throwable, tag: String? = null) {
        val stackTrace = throwable.stackTraceToString().take(500)
        log(
            "${throwable.javaClass.simpleName}: ${throwable.message}\n$stackTrace",
            LogLevel.ERROR,
            tag
        )
    }

    fun clear() {
        synchronized(this) {
            _entries.value = emptyList()
        }
        info("Logs cleared", "DebugLog")
    }

    fun copyableText(): String {
        return _entries.value.joinToString("\n") { it.formatted() }
    }

    val entryCount: Int
        get() = _entries.value.size
}
