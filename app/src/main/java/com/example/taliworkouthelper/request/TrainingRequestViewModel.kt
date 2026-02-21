package com.example.taliworkouthelper.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainingRequestFormUiState(
    val partnerUidInput: String = "",
    val startInput: String = "",
    val endInput: String = ""
)

data class TrainingRequestUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val notificationCount: Int = 0,
    val incoming: List<TrainingRequest> = emptyList(),
    val outgoing: List<TrainingRequest> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val form: TrainingRequestFormUiState = TrainingRequestFormUiState()
)

class TrainingRequestViewModel(
    private val repository: TrainingRequestRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(TrainingRequestUiState())
    val state: StateFlow<TrainingRequestUiState> = mutableState.asStateFlow()

    init {
        observeIncomingRequests()
        observeOutgoingRequests()
        observeBookings()
    }

    fun onPartnerUidChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(partnerUidInput = value), errorMessage = null)
        }
    }

    fun onStartChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(startInput = value), errorMessage = null)
        }
    }

    fun onEndChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(endInput = value), errorMessage = null)
        }
    }

    fun sendRequest() {
        val form = state.value.form
        val validation = validateForm(form)
        if (validation.isFailure) {
            mutableState.update { current ->
                current.copy(errorMessage = validation.exceptionOrNull()?.message ?: "Invalid request")
            }
            return
        }

        val valid = validation.getOrThrow()
        viewModelScope.launch {
            mutableState.update { current -> current.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.sendRequest(valid.partnerUid, valid.startEpochMillis, valid.endEpochMillis)
            mutableState.update { current ->
                if (result.isSuccess) {
                    current.copy(
                        isSubmitting = false,
                        errorMessage = null,
                        form = TrainingRequestFormUiState()
                    )
                } else {
                    current.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to send request"
                    )
                }
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.acceptRequest(requestId)
            result.exceptionOrNull()?.let { throwable ->
                mutableState.update { current ->
                    current.copy(errorMessage = throwable.message ?: "Failed to accept request")
                }
            }
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.declineRequest(requestId)
            result.exceptionOrNull()?.let { throwable ->
                mutableState.update { current ->
                    current.copy(errorMessage = throwable.message ?: "Failed to decline request")
                }
            }
        }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun observeIncomingRequests() {
        viewModelScope.launch {
            repository.observeIncomingRequests().collect { incoming ->
                mutableState.update { current ->
                    current.copy(
                        isLoading = false,
                        incoming = incoming,
                        notificationCount = incoming.size
                    )
                }
            }
        }
    }

    private fun observeOutgoingRequests() {
        viewModelScope.launch {
            repository.observeOutgoingRequests().collect { outgoing ->
                mutableState.update { current ->
                    current.copy(isLoading = false, outgoing = outgoing)
                }
            }
        }
    }

    private fun observeBookings() {
        viewModelScope.launch {
            repository.observeBookings().collect { bookings ->
                mutableState.update { current ->
                    current.copy(isLoading = false, bookings = bookings)
                }
            }
        }
    }

    private data class ValidRequestInput(
        val partnerUid: String,
        val startEpochMillis: Long,
        val endEpochMillis: Long
    )

    /**
     * Validates request proposal input before repository calls.
     *
     * Input datetime format: yyyy-MM-ddTHH:mm (ISO local datetime)
     */
    private fun validateForm(form: TrainingRequestFormUiState): Result<ValidRequestInput> {
        val partnerUid = form.partnerUidInput.trim()
        if (partnerUid.isBlank()) {
            return Result.failure(IllegalArgumentException("Partner uid is required"))
        }

        val start = parseDateTime(form.startInput)
            ?: return Result.failure(IllegalArgumentException("Start datetime is invalid"))
        val end = parseDateTime(form.endInput)
            ?: return Result.failure(IllegalArgumentException("End datetime is invalid"))

        if (end <= start) {
            return Result.failure(IllegalArgumentException("End time must be after start time"))
        }

        return Result.success(
            ValidRequestInput(
                partnerUid = partnerUid,
                startEpochMillis = start,
                endEpochMillis = end
            )
        )
    }

    private fun parseDateTime(value: String): Long? {
        return try {
            val localDateTime = LocalDateTime.parse(value.trim())
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatEpoch(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .toString()
    }
}
