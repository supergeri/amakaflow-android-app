package com.amakaflow.companion.domain.usecase.pairing

import com.amakaflow.companion.data.model.PairingResponse
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.repository.PairingRepository
import javax.inject.Inject

/**
 * Use case for pairing the device with an AmakaFlow account.
 */
class PairDeviceUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    /**
     * Pair the device using a 6-character short code or full token.
     *
     * @param code The pairing code (6 characters) or full token.
     * @return Result containing PairingResponse on success, or error message.
     */
    suspend operator fun invoke(code: String): Result<PairingResponse> {
        return pairingRepository.pair(code)
    }
}
