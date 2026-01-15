package com.amakaflow.companion.domain.usecase.pairing

import com.amakaflow.companion.data.model.UserProfile
import com.amakaflow.companion.domain.repository.PairingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Data class representing the current pairing state.
 */
data class PairingState(
    val isPaired: Boolean,
    val userProfile: UserProfile?,
    val needsReauth: Boolean
)

/**
 * Use case for loading and observing the current pairing state.
 */
class LoadPairingStateUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    /**
     * Get the StateFlow indicating whether the device is paired.
     */
    val isPaired: StateFlow<Boolean>
        get() = pairingRepository.isPaired

    /**
     * Get the StateFlow containing the user profile.
     */
    val userProfile: StateFlow<UserProfile?>
        get() = pairingRepository.userProfile

    /**
     * Get the StateFlow indicating whether re-authentication is needed.
     */
    val needsReauth: StateFlow<Boolean>
        get() = pairingRepository.needsReauth

    /**
     * Get the current pairing state as a snapshot.
     */
    operator fun invoke(): PairingState {
        return PairingState(
            isPaired = pairingRepository.isPaired.value,
            userProfile = pairingRepository.userProfile.value,
            needsReauth = pairingRepository.needsReauth.value
        )
    }
}
