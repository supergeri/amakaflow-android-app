@file:Suppress("DEPRECATION")

package com.amakaflow.companion.data.repository

// Type alias for backward compatibility during migration
// Use CompletionRepositoryImpl and inject via domain.repository.CompletionRepository interface
@Deprecated(
    message = "Use CompletionRepositoryImpl and inject via domain.repository.CompletionRepository interface",
    replaceWith = ReplaceWith("CompletionRepositoryImpl")
)
typealias CompletionQueueRepository = CompletionRepositoryImpl
