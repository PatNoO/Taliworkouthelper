package com.example.taliworkouthelper.session

import kotlinx.coroutines.flow.Flow

interface WorkoutSessionRepository {
    fun observeActiveSession(): Flow<WorkoutSession?>
    fun observeCompletedSessions(limit: Int = 20): Flow<List<WorkoutSession>>
    suspend fun saveActiveSession(session: WorkoutSession): Result<WorkoutSession>
    suspend fun completeSession(session: WorkoutSession): Result<WorkoutSession>
}
