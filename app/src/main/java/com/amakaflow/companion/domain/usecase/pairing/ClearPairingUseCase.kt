package com.amakaflow.companion.domain.usecase.pairing

import com.amakaflow.companion.domain.repository.PairingRepository
import javax.inject.Inject

/**
 * Use case for clearing the device pairing (unpair).
 */
class ClearPairingUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    /**
     * Unpair the device and clear all stored credentials.
     */
    operator fun invoke() {
        pairingRepository.unpair()
    }
}
