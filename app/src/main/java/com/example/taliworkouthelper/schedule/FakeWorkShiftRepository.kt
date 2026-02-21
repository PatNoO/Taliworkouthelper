package com.example.taliworkouthelper.schedule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkShiftRepository : WorkScheduleRepository {
    private val shiftsState = MutableStateFlow<List<WorkShift>>(emptyList())

    override fun getShifts(): Flow<List<WorkShift>> = shiftsState

    override suspend fun addShift(shift: WorkShift): Result<WorkShift> {
        shiftsState.value = shiftsState.value + shift
        return Result.success(shift)
    }

    override suspend fun updateShift(shift: WorkShift): Result<WorkShift> {
        shiftsState.value = shiftsState.value.map { existing ->
            if (existing.id == shift.id) shift else existing
        }
        return Result.success(shift)
    }

    override suspend fun removeShift(id: String) {
        shiftsState.value = shiftsState.value.filterNot { it.id == id }
    }
}
