package com.example.taliworkouthelper.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TemplateExerciseFormUiState(
    val nameInput: String = "",
    val setsInput: String = "",
    val repsInput: String = ""
)

data class WorkoutTemplateFormUiState(
    val editingTemplateId: String? = null,
    val titleInput: String = "",
    val exerciseForm: TemplateExerciseFormUiState = TemplateExerciseFormUiState(),
    val exercises: List<TemplateExercise> = emptyList()
) {
    val isEditing: Boolean = editingTemplateId != null
}

data class WorkoutTemplateUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val templates: List<WorkoutTemplate> = emptyList(),
    val form: WorkoutTemplateFormUiState = WorkoutTemplateFormUiState()
) {
    val isEmpty: Boolean = !isLoading && templates.isEmpty()
}

class WorkoutTemplateViewModel(
    private val repository: WorkoutTemplateRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(WorkoutTemplateUiState())
    val state: StateFlow<WorkoutTemplateUiState> = mutableState.asStateFlow()

    init {
        observeTemplates()
    }

    fun onTitleChanged(value: String) {
        mutableState.update { current ->
            current.copy(form = current.form.copy(titleInput = value), errorMessage = null)
        }
    }

    fun onExerciseNameChanged(value: String) {
        mutableState.update { current ->
            current.copy(
                form = current.form.copy(
                    exerciseForm = current.form.exerciseForm.copy(nameInput = value)
                ),
                errorMessage = null
            )
        }
    }

    fun onExerciseSetsChanged(value: String) {
        mutableState.update { current ->
            current.copy(
                form = current.form.copy(
                    exerciseForm = current.form.exerciseForm.copy(setsInput = value)
                ),
                errorMessage = null
            )
        }
    }

    fun onExerciseRepsChanged(value: String) {
        mutableState.update { current ->
            current.copy(
                form = current.form.copy(
                    exerciseForm = current.form.exerciseForm.copy(repsInput = value)
                ),
                errorMessage = null
            )
        }
    }

    fun addExerciseToForm() {
        val exercise = validateExerciseForm(state.value.form.exerciseForm)
        if (exercise.isFailure) {
            mutableState.update { current ->
                current.copy(errorMessage = exercise.exceptionOrNull()?.message ?: "Invalid exercise")
            }
            return
        }

        val validExercise = exercise.getOrThrow()
        mutableState.update { current ->
            current.copy(
                form = current.form.copy(
                    exercises = current.form.exercises + validExercise,
                    exerciseForm = TemplateExerciseFormUiState()
                ),
                errorMessage = null
            )
        }
    }

    fun removeExerciseFromForm(index: Int) {
        mutableState.update { current ->
            current.copy(
                form = current.form.copy(
                    exercises = current.form.exercises.filterIndexed { idx, _ -> idx != index }
                )
            )
        }
    }

    fun onEditTemplate(template: WorkoutTemplate) {
        mutableState.update { current ->
            current.copy(
                form = WorkoutTemplateFormUiState(
                    editingTemplateId = template.id,
                    titleInput = template.title,
                    exercises = template.exercises
                ),
                errorMessage = null
            )
        }
    }

    fun onCancelEdit() {
        mutableState.update { current ->
            current.copy(form = WorkoutTemplateFormUiState(), errorMessage = null)
        }
    }

    fun onSaveTemplate() {
        val formSnapshot = state.value.form
        val validation = validateTemplateForm(formSnapshot)
        if (validation.isFailure) {
            mutableState.update { current ->
                current.copy(errorMessage = validation.exceptionOrNull()?.message ?: "Invalid template")
            }
            return
        }

        val validTemplate = validation.getOrThrow()
        viewModelScope.launch {
            mutableState.update { current -> current.copy(isSaving = true, errorMessage = null) }
            val result = if (formSnapshot.isEditing) {
                repository.updateTemplate(validTemplate)
            } else {
                repository.addTemplate(validTemplate)
            }

            mutableState.update { current ->
                if (result.isSuccess) {
                    current.copy(isSaving = false, form = WorkoutTemplateFormUiState(), errorMessage = null)
                } else {
                    current.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Unable to save template"
                    )
                }
            }
        }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    /**
     * Validates template save input before persistence.
     *
     * Why:
     * Enforces US8 acceptance criteria and keeps business rules out of the UI layer.
     */
    private fun validateTemplateForm(form: WorkoutTemplateFormUiState): Result<WorkoutTemplate> {
        val title = form.titleInput.trim()
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Template title is required"))
        }
        if (form.exercises.isEmpty()) {
            return Result.failure(IllegalArgumentException("Template must include at least one exercise"))
        }

        return Result.success(
            WorkoutTemplate(
                id = form.editingTemplateId ?: UUID.randomUUID().toString(),
                title = title,
                exercises = form.exercises
            )
        )
    }

    private fun validateExerciseForm(form: TemplateExerciseFormUiState): Result<TemplateExercise> {
        val name = form.nameInput.trim()
        val sets = form.setsInput.toIntOrNull()
        val reps = form.repsInput.toIntOrNull()

        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Exercise name is required"))
        }
        if (sets == null || sets <= 0) {
            return Result.failure(IllegalArgumentException("Sets must be a positive number"))
        }
        if (reps == null || reps <= 0) {
            return Result.failure(IllegalArgumentException("Reps must be a positive number"))
        }

        return Result.success(TemplateExercise(name = name, sets = sets, reps = reps))
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            repository.getTemplates().collect { templates ->
                mutableState.update { current ->
                    current.copy(isLoading = false, templates = templates, errorMessage = null)
                }
            }
        }
    }
}
