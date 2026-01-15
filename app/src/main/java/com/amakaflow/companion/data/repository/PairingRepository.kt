@file:Suppress("DEPRECATION")

package com.amakaflow.companion.data.repository

// Type alias for backward compatibility during migration
// Use PairingRepositoryImpl and inject via domain.repository.PairingRepository interface
@Deprecated(
    message = "Use PairingRepositoryImpl and inject via domain.repository.PairingRepository interface",
    replaceWith = ReplaceWith("PairingRepositoryImpl")
)
typealias PairingRepository = PairingRepositoryImpl

// Re-export PairingError for backward compatibility
// (PairingError is defined in PairingRepositoryImpl.kt)
