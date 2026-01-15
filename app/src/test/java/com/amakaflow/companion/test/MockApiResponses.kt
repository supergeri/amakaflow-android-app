package com.amakaflow.companion.test

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy

/**
 * Helper utilities for setting up mock API responses in tests.
 */
object MockApiResponses {

    /**
     * Create a successful workouts response.
     */
    fun successWorkoutsResponse(workoutsJson: String = "[]") = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success": true, "workouts": $workoutsJson}""")

    /**
     * Create a successful completions response.
     */
    fun successCompletionsResponse(completionsJson: String = "[]") = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success": true, "completions": $completionsJson}""")

    /**
     * Create a generic success response with custom body.
     */
    fun successResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    /**
     * Create an error response.
     */
    fun errorResponse(code: Int = 500, message: String = "Server Error") = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success": false, "message": "$message"}""")

    /**
     * Create a 401 Unauthorized response.
     */
    fun unauthorizedResponse(message: String = "Unauthorized") = MockResponse()
        .setResponseCode(401)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success": false, "message": "$message"}""")

    /**
     * Create a 404 Not Found response.
     */
    fun notFoundResponse(message: String = "Not Found") = MockResponse()
        .setResponseCode(404)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"success": false, "message": "$message"}""")

    /**
     * Create a response that simulates a network error (connection drops).
     */
    fun networkError() = MockResponse()
        .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)

    /**
     * Create a response that simulates a timeout.
     */
    fun timeoutResponse() = MockResponse()
        .setSocketPolicy(SocketPolicy.NO_RESPONSE)

    /**
     * Create a slow response for testing timeouts.
     */
    fun slowResponse(body: String, delayMs: Long = 5000) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
        .setBodyDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
}

/**
 * Extension for enqueuing a success response.
 */
fun MockWebServer.enqueueSuccess(body: String) {
    enqueue(
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
    )
}

/**
 * Extension for enqueuing an error response.
 */
fun MockWebServer.enqueueError(code: Int, message: String) {
    enqueue(
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"success": false, "message": "$message"}""")
    )
}

/**
 * Extension for enqueuing a network error.
 */
fun MockWebServer.enqueueNetworkError() {
    enqueue(
        MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
    )
}
