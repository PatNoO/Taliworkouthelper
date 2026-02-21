package com.example.taliworkouthelper.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetEntryFormUiState(
    val exerciseNameInput: String = "",
    val repsInput: String = "",
    val weightInput: String = "",
    val exerciseNoteInput: String = ""
)

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val activeSession: WorkoutSession? = null,
    val history: List<WorkoutSession> = emptyList(),
    val setEntryForm: SetEntryFormUiState = SetEntryFormUiState()
) {
    val hasActiveLogs: Boolean = activeSession?.exerciseLogs?.isNotEmpty() == true
}

class ActiveWorkoutViewModel(
    private val repository: WorkoutSessionRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ActiveWorkoutUiState())
    val state: StateFlow<ActiveWorkoutUiState> = mutableState.asStateFlow()

    init {
        observeActiveSession()
        observeHistory()
    }

    fun onExerciseNameChanged(value: String) {
        mutableState.update { current ->
            current.copy(setEntryForm = current.setEntryForm.copy(exerciseNameInput = value), errorMessage = null)
        }
    }

    fun onRepsChanged(value: String) {
        mutableState.update { current ->
            current.copy(setEntryForm = current.setEntryForm.copy(repsInput = value), errorMessage = null)
        }
    }

    fun onWeightChanged(value: String) {
        mutableState.update { current ->
            current.copy(setEntryForm = current.setEntryForm.copy(weightInput = value), errorMessage = null)
        }
    }

    fun onExerciseNoteChanged(value: String) {
        mutableState.update { current ->
            current.copy(setEntryForm = current.setEntryForm.copy(exerciseNoteInput = value), errorMessage = null)
        }
    }

    fun onGeneralNoteChanged(value: String) {
        val session = state.value.activeSession ?: createEmptyActiveSession()
        val updated = session.copy(generalNote = value)
        mutableState.update { current ->
            current.copy(activeSession = updated, errorMessage = null)
        }
        autosave(updated)
    }

    fun addSetToActiveSession() {
        val form = state.value.setEntryForm
        val validation = validateSetEntry(form)
        if (validation.isFailure) {
            mutableState.update { current ->
                current.copy(errorMessage = validation.exceptionOrNull()?.message ?: "Invalid set")
            }
            return
        }

        val validSet = validation.getOrThrow()
        val session = state.value.activeSession ?: createEmptyActiveSession()

        val existingExerciseIndex = session.exerciseLogs.indexOfFirst {
            it.exerciseName.equals(validSet.exerciseName, ignoreCase = true)
        }

        val updatedLogs = if (existingExerciseIndex >= 0) {
            session.exerciseLogs.mapIndexed { index, existing ->
                if (index == existingExerciseIndex) {
                    existing.copy(
                        sets = existing.sets + validSet.set,
                        note = validSet.note.ifBlank { existing.note }
                    )
                } else {
                    existing
                }
            }
        } else {
            session.exerciseLogs + ExerciseSessionLog(
                exerciseName = validSet.exerciseName,
                sets = listOf(validSet.set),
                note = validSet.note
            )
        }

        val updatedSession = session.copy(exerciseLogs = updatedLogs)
        mutableState.update { current ->
            current.copy(
                activeSession = updatedSession,
                setEntryForm = SetEntryFormUiState(),
                errorMessage = null
            )
        }

        autosave(updatedSession)
    }

    fun completeActiveSession() {
        val active = state.value.activeSession
        if (active == null || active.exerciseLogs.isEmpty()) {
            mutableState.update { current ->
                current.copy(errorMessage = "Cannot complete an empty session")
            }
            return
        }

        viewModelScope.launch {
            mutableState.update { current -> current.copy(isSaving = true, errorMessage = null) }
            val result = repository.completeSession(active)
            mutableState.update { current ->
                if (result.isSuccess) {
                    current.copy(isSaving = false, activeSession = null, errorMessage = null)
                } else {
                    current.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to complete session"
                    )
                }
            }
        }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun autosave(session: WorkoutSession) {
        viewModelScope.launch {
            val result = repository.saveActiveSession(session)
            result.exceptionOrNull()?.let { throwable ->
                mutableState.update { current ->
                    current.copy(errorMessage = throwable.message ?: "Autosave failed")
                }
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            repository.observeActiveSession().collect { active ->
                mutableState.update { current ->
                    current.copy(
                        isLoading = false,
                        activeSession = active,
                        errorMessage = current.errorMessage
                    )
                }
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.observeCompletedSessions().collect { completed ->
                mutableState.update { current ->
                    current.copy(isLoading = false, history = completed)
                }
            }
        }
    }

    private data class ValidSetEntry(
        val exerciseName: String,
        val set: LoggedSet,
        val note: String
    )

    /**
     * Validates logging input before mutating active session state.
     *
     * Why:
     * Keeps session business rules outside Compose and guarantees
     * only valid sets are persisted during autosave.
     */
    private fun validateSetEntry(form: SetEntryFormUiState): Result<ValidSetEntry> {
        val exerciseName = form.exerciseNameInput.trim()
        if (exerciseName.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise name is required"))
        }

        val reps = form.repsInput.toIntOrNull()
        if (reps == null || reps <= 0) {
            return Result.failure(IllegalArgumentException("Reps must be a positive number"))
        }

        val weight = form.weightInput.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
        if (form.weightInput.isNotBlank() && weight == null) {
            return Result.failure(IllegalArgumentException("Weight must be a number"))
        }

        return Result.success(
            ValidSetEntry(
                exerciseName = exerciseName,
                set = LoggedSet(reps = reps, weightKg = weight),
                note = form.exerciseNoteInput.trim()
            )
        )
    }

    private fun createEmptyActiveSession(): WorkoutSession {
        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            status = WorkoutSessionStatus.IN_PROGRESS,
            exerciseLogs = emptyList(),
            generalNote = ""
        )
    }
}
