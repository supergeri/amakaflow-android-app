package com.amakaflow.companion.debug

/**
 * Global exception handler that captures uncaught exceptions to the debug log.
 * This allows viewing crash information on-device without needing Android Studio.
 */
class GlobalExceptionHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        DebugLog.error("UNCAUGHT EXCEPTION on thread: ${thread.name}", "Crash")
        DebugLog.error(throwable, "Crash")

        // Let the default handler (or Sentry) handle the crash
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        /**
         * Install the global exception handler.
         * Call this in Application.onCreate().
         */
        fun install() {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(defaultHandler)
            )
            DebugLog.info("Global exception handler installed", "DebugLog")
        }
    }
}
