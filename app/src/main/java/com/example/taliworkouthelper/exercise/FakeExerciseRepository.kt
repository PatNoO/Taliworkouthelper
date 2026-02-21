package com.example.taliworkouthelper.exercise

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExerciseRepository(
    initialExercises: List<Exercise> = emptyList()
) : ExerciseRepository {
    private val exercises = MutableStateFlow(initialExercises)

    override fun observeExercises(): Flow<List<Exercise>> = exercises

    fun setExercises(items: List<Exercise>) {
        exercises.value = items
    }
}
