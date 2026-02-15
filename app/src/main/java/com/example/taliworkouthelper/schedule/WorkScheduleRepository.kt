package com.example.taliworkouthelper.schedule

import kotlinx.coroutines.flow.Flow

interface WorkScheduleRepository {
    fun getShifts(): Flow<List<WorkShift>>
    suspend fun addShift(shift: WorkShift): Result<WorkShift>
    suspend fun removeShift(id: String)
}
