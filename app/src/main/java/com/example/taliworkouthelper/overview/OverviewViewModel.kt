package com.example.taliworkouthelper.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taliworkouthelper.session.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OverviewUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val upcomingBookings: List<UpcomingBooking> = emptyList(),
    val workoutHistory: List<WorkoutSession> = emptyList(),
    val selectedSession: WorkoutSession? = null
) {
    val isEmptyBookings: Boolean = !isLoading && upcomingBookings.isEmpty()
    val isEmptyWorkoutHistory: Boolean = !isLoading && workoutHistory.isEmpty()
}

class OverviewViewModel(private val repository: OverviewRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = mutableState.asStateFlow()

    private var bookingsLoaded = false
    private var historyLoaded = false

    init {
        observeUpcomingBookings()
        observeWorkoutHistory()
    }

    fun selectSession(sessionId: String) {
        mutableState.update { current ->
            current.copy(selectedSession = current.workoutHistory.firstOrNull { it.id == sessionId })
        }
    }

    fun clearSelectedSession() {
        mutableState.update { current -> current.copy(selectedSession = null) }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun observeUpcomingBookings() {
        viewModelScope.launch {
            runCatching {
                repository.observeUpcomingBookings().collect { bookings ->
                    bookingsLoaded = true
                    mutableState.update { current ->
                        current.copy(
                            isLoading = !historyLoaded,
                            upcomingBookings = bookings,
                            errorMessage = null
                        )
                    }
                }
            }.onFailure { throwable ->
                bookingsLoaded = true
                mutableState.update { current ->
                    current.copy(
                        isLoading = !historyLoaded,
                        errorMessage = throwable.message ?: "Unable to load bookings"
                    )
                }
            }
        }
    }

    /**
     * Keeps selected detail synchronized with history updates.
     *
     * Why:
     * Ensures detail view reflects latest data and clears stale selection
     * if a session is no longer present in loaded history.
     */
    private fun observeWorkoutHistory() {
        viewModelScope.launch {
            runCatching {
                repository.observeWorkoutHistory().collect { history ->
                    historyLoaded = true
                    mutableState.update { current ->
                        val selectedId = current.selectedSession?.id
                        current.copy(
                            isLoading = !bookingsLoaded,
                            workoutHistory = history,
                            selectedSession = selectedId?.let { id ->
                                history.firstOrNull { it.id == id }
                            },
                            errorMessage = null
                        )
                    }
                }
            }.onFailure { throwable ->
                historyLoaded = true
                mutableState.update { current ->
                    current.copy(
                        isLoading = !bookingsLoaded,
                        errorMessage = throwable.message ?: "Unable to load workout history"
                    )
                }
            }
        }
    }
}
