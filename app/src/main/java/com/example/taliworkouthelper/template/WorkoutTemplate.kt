package com.example.taliworkouthelper.template

data class TemplateExercise(
    val name: String,
    val sets: Int,
    val reps: Int
)

data class WorkoutTemplate(
    val id: String,
    val title: String,
    val exercises: List<TemplateExercise>
)
