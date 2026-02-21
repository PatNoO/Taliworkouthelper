package com.example.taliworkouthelper.exercise

import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExercises(): Flow<List<Exercise>>
}
