package com.example.taliworkouthelper.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseLibraryUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedMuscleGroup: String = ALL_FILTER,
    val allExercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val muscleGroupFilters: List<String> = listOf(ALL_FILTER),
    val selectedExercise: Exercise? = null
) {
    val isEmpty: Boolean = !isLoading && filteredExercises.isEmpty()

    companion object {
        const val ALL_FILTER = "All"
    }
}

class ExerciseLibraryViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(ExerciseLibraryUiState())
    val state: StateFlow<ExerciseLibraryUiState> = mutableState.asStateFlow()

    init {
        observeExercises()
    }

    fun onSearchQueryChanged(value: String) {
        mutableState.update { current ->
            val filtered = filterExercises(
                exercises = current.allExercises,
                query = value,
                muscleGroup = current.selectedMuscleGroup
            )
            current.copy(searchQuery = value, filteredExercises = filtered, errorMessage = null)
        }
    }

    fun onSelectMuscleGroup(group: String) {
        mutableState.update { current ->
            val filtered = filterExercises(
                exercises = current.allExercises,
                query = current.searchQuery,
                muscleGroup = group
            )
            current.copy(selectedMuscleGroup = group, filteredExercises = filtered, errorMessage = null)
        }
    }

    fun onOpenExerciseDetail(exerciseId: String) {
        mutableState.update { current ->
            current.copy(selectedExercise = current.allExercises.firstOrNull { it.id == exerciseId })
        }
    }

    fun onCloseExerciseDetail() {
        mutableState.update { current -> current.copy(selectedExercise = null) }
    }

    fun dismissError() {
        mutableState.update { current -> current.copy(errorMessage = null) }
    }

    private fun observeExercises() {
        viewModelScope.launch {
            runCatching {
                repository.observeExercises().collect { exercises ->
                    mutableState.update { current ->
                        val filters = listOf(ALL_FILTER) +
                            exercises.map { it.muscleGroup }.distinct().sorted()
                        val selectedFilter = current.selectedMuscleGroup.takeIf { filters.contains(it) } ?: ALL_FILTER
                        val filtered = filterExercises(
                            exercises = exercises,
                            query = current.searchQuery,
                            muscleGroup = selectedFilter
                        )
                        val selectedExerciseId = current.selectedExercise?.id

                        current.copy(
                            isLoading = false,
                            errorMessage = null,
                            allExercises = exercises,
                            filteredExercises = filtered,
                            muscleGroupFilters = filters,
                            selectedMuscleGroup = selectedFilter,
                            selectedExercise = selectedExerciseId?.let { id ->
                                exercises.firstOrNull { it.id == id }
                            }
                        )
                    }
                }
            }.onFailure { throwable ->
                mutableState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load exercises"
                    )
                }
            }
        }
    }

    /**
     * Applies UI filters in ViewModel to keep composables stateless.
     *
     * Why:
     * Keeps business filtering logic out of the UI layer and makes it unit-testable.
     */
    private fun filterExercises(
        exercises: List<Exercise>,
        query: String,
        muscleGroup: String
    ): List<Exercise> {
        val normalizedQuery = query.trim().lowercase()
        return exercises.filter { exercise ->
            val matchesGroup = muscleGroup == ALL_FILTER || exercise.muscleGroup.equals(muscleGroup, ignoreCase = true)
            val matchesSearch = normalizedQuery.isBlank() ||
                exercise.name.lowercase().contains(normalizedQuery) ||
                exercise.description.lowercase().contains(normalizedQuery) ||
                exercise.equipment.lowercase().contains(normalizedQuery)
            matchesGroup && matchesSearch
        }
    }

    private companion object {
        const val ALL_FILTER = ExerciseLibraryUiState.ALL_FILTER
    }
}
