package com.amakaflow.companion.domain.usecase.pairing

import com.amakaflow.companion.data.model.UserProfile
import com.amakaflow.companion.domain.repository.PairingRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class LoadPairingStateUseCaseTest {

    private lateinit var useCase: LoadPairingStateUseCase
    private lateinit var mockRepository: PairingRepository

    private val isPairedFlow = MutableStateFlow(false)
    private val userProfileFlow = MutableStateFlow<UserProfile?>(null)
    private val needsReauthFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        mockRepository = mockk()
        every { mockRepository.isPaired } returns isPairedFlow
        every { mockRepository.userProfile } returns userProfileFlow
        every { mockRepository.needsReauth } returns needsReauthFlow

        useCase = LoadPairingStateUseCase(mockRepository)
    }

    @Test
    fun `invoke returns not paired when device is not paired`() {
        // Given
        isPairedFlow.value = false
        userProfileFlow.value = null
        needsReauthFlow.value = false

        // When
        val result = useCase()

        // Then
        assertThat(result.isPaired).isFalse()
        assertThat(result.userProfile).isNull()
        assertThat(result.needsReauth).isFalse()
    }

    @Test
    fun `invoke returns paired with user profile when device is paired`() {
        // Given
        val userProfile = UserProfile(
            id = "user-123",
            email = "test@example.com",
            name = "Test User"
        )
        isPairedFlow.value = true
        userProfileFlow.value = userProfile
        needsReauthFlow.value = false

        // When
        val result = useCase()

        // Then
        assertThat(result.isPaired).isTrue()
        assertThat(result.userProfile).isEqualTo(userProfile)
        assertThat(result.userProfile?.name).isEqualTo("Test User")
        assertThat(result.needsReauth).isFalse()
    }

    @Test
    fun `invoke returns needsReauth when authentication is invalid`() {
        // Given
        isPairedFlow.value = true
        userProfileFlow.value = null
        needsReauthFlow.value = true

        // When
        val result = useCase()

        // Then
        assertThat(result.isPaired).isTrue()
        assertThat(result.needsReauth).isTrue()
    }

    @Test
    fun `isPaired StateFlow exposes repository state`() {
        // Given
        isPairedFlow.value = true

        // When/Then
        assertThat(useCase.isPaired.value).isTrue()

        // When
        isPairedFlow.value = false

        // Then
        assertThat(useCase.isPaired.value).isFalse()
    }

    @Test
    fun `userProfile StateFlow exposes repository state`() {
        // Given
        val userProfile = UserProfile(
            id = "user-456",
            email = "another@example.com",
            name = "Another User"
        )
        userProfileFlow.value = userProfile

        // When/Then
        assertThat(useCase.userProfile.value).isEqualTo(userProfile)
    }

    @Test
    fun `needsReauth StateFlow exposes repository state`() {
        // Given
        needsReauthFlow.value = true

        // When/Then
        assertThat(useCase.needsReauth.value).isTrue()
    }
}
