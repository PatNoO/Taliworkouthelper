package com.example.taliworkouthelper.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkoutSessionRepository : WorkoutSessionRepository {
    private val activeSessionFlow = MutableStateFlow<WorkoutSession?>(null)
    private val historyFlow = MutableStateFlow<List<WorkoutSession>>(emptyList())

    override fun observeActiveSession(): Flow<WorkoutSession?> = activeSessionFlow

    override fun observeCompletedSessions(limit: Int): Flow<List<WorkoutSession>> = historyFlow

    override suspend fun saveActiveSession(session: WorkoutSession): Result<WorkoutSession> {
        activeSessionFlow.value = session.copy(status = WorkoutSessionStatus.IN_PROGRESS)
        return Result.success(activeSessionFlow.value!!)
    }

    override suspend fun completeSession(session: WorkoutSession): Result<WorkoutSession> {
        val completed = session.copy(status = WorkoutSessionStatus.COMPLETED)
        activeSessionFlow.value = null
        historyFlow.value = listOf(completed) + historyFlow.value
        return Result.success(completed)
    }
}
