package com.example.taliworkouthelper.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_HOUR = 0
private const val MAX_HOUR = 23

data class ShiftFormUiState(
    val editingShiftId: String? = null,
    val startHourInput: String = "",
    val endHourInput: String = ""
) {
    val isEditing: Boolean = editingShiftId != null
}

data class WorkScheduleUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val shifts: List<WorkShift> = emptyList(),
    val form: ShiftFormUiState = ShiftFormUiState()
) {
    val isEmpty: Boolean = !isLoading && shifts.isEmpty()
}

class WorkScheduleViewModel(private val repo: WorkScheduleRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(WorkScheduleUiState())
    val state: StateFlow<WorkScheduleUiState> = mutableState.asStateFlow()

    init {
        observeShifts()
    }

    fun onStartHourChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(startHourInput = value), errorMessage = null)
        }
    }

    fun onEndHourChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(endHourInput = value), errorMessage = null)
        }
    }

    fun onEditShift(shift: WorkShift) {
        mutableState.update { current ->
            current.copy(
                form = ShiftFormUiState(
                    editingShiftId = shift.id,
                    startHourInput = shift.startHour.toString(),
                    endHourInput = shift.endHour.toString()
                ),
                errorMessage = null
            )
        }
    }

    fun onCancelEdit() {
        mutableState.update { current ->
            current.copy(form = ShiftFormUiState(), errorMessage = null)
        }
    }

    fun onSubmitShift() {
        val formSnapshot = mutableState.value.form
        val validation = validateForm(formSnapshot)
        if (validation.isFailure) {
            mutableState.update { current ->
                current.copy(errorMessage = validation.exceptionOrNull()?.message)
            }
            return
        }

        val validData = validation.getOrThrow()
        val shift = WorkShift(
            id = formSnapshot.editingShiftId ?: UUID.randomUUID().toString(),
            startHour = validData.startHour,
            endHour = validData.endHour
        )

        viewModelScope.launch {
            mutableState.update { current -> current.copy(isSaving = true, errorMessage = null) }
            val result = if (formSnapshot.isEditing) repo.updateShift(shift) else repo.addShift(shift)
            mutableState.update { current ->
                if (result.isSuccess) {
                    current.copy(isSaving = false, form = ShiftFormUiState(), errorMessage = null)
                } else {
                    current.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Unable to save shift"
                    )
                }
            }
        }
    }

    fun removeShift(id: String) {
        viewModelScope.launch {
            runCatching { repo.removeShift(id) }
                .onFailure { throwable ->
                    mutableState.update { current ->
                        current.copy(errorMessage = throwable.message ?: "Unable to delete shift")
                    }
                }
        }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun observeShifts() {
        viewModelScope.launch {
            repo.getShifts().collect { shifts ->
                mutableState.update { current ->
                    current.copy(
                        isLoading = false,
                        shifts = shifts,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private data class ValidShiftData(val startHour: Int, val endHour: Int)

    /**
     * Validates form inputs before create/update operations.
     *
     * Why:
     * Prevents invalid ranges from being persisted and keeps
     * business rules out of the composable layer.
     */
    private fun validateForm(form: ShiftFormUiState): Result<ValidShiftData> {
        val startHour = form.startHourInput.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Start hour must be a number"))
        val endHour = form.endHourInput.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("End hour must be a number"))

        if (startHour !in MIN_HOUR..MAX_HOUR || endHour !in MIN_HOUR..MAX_HOUR) {
            return Result.failure(IllegalArgumentException("Hours must be between 0 and 23"))
        }

        if (endHour <= startHour) {
            return Result.failure(IllegalArgumentException("End hour must be after start hour"))
        }

        return Result.success(ValidShiftData(startHour, endHour))
    }
}
