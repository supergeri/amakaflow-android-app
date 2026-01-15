@file:Suppress("DEPRECATION")

package com.amakaflow.companion.data.repository

import com.amakaflow.companion.domain.repository.CompletionsResult

// Type alias for backward compatibility during migration
// Use com.amakaflow.companion.domain.Result in new code
@Deprecated(
    message = "Use com.amakaflow.companion.domain.Result instead",
    replaceWith = ReplaceWith("Result", "com.amakaflow.companion.domain.Result")
)
typealias Result<T> = com.amakaflow.companion.domain.Result<T>

// Re-export for backward compatibility
@Deprecated(
    message = "Use com.amakaflow.companion.domain.repository.CompletionsResult instead",
    replaceWith = ReplaceWith("CompletionsResult", "com.amakaflow.companion.domain.repository.CompletionsResult")
)
typealias DeprecatedCompletionsResult = CompletionsResult

// Type alias for backward compatibility - existing code can still use WorkoutRepository
@Deprecated(
    message = "Use WorkoutRepositoryImpl and inject via domain.repository.WorkoutRepository interface",
    replaceWith = ReplaceWith("WorkoutRepositoryImpl")
)
typealias WorkoutRepository = WorkoutRepositoryImpl
