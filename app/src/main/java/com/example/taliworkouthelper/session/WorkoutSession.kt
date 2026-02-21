package com.example.taliworkouthelper.session

enum class WorkoutSessionStatus {
    IN_PROGRESS,
    COMPLETED
}

data class LoggedSet(
    val reps: Int,
    val weightKg: Double?
)

data class ExerciseSessionLog(
    val exerciseName: String,
    val sets: List<LoggedSet>,
    val note: String = ""
)

data class WorkoutSession(
    val id: String,
    val status: WorkoutSessionStatus,
    val exerciseLogs: List<ExerciseSessionLog>,
    val generalNote: String = ""
)
