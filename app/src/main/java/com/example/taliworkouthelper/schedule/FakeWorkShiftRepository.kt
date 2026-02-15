package com.example.taliworkouthelper.schedule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkShiftRepository : WorkScheduleRepository {
    private val _shifts = MutableStateFlow<List<WorkShift>>(emptyList())

    override fun getShifts(): Flow<List<WorkShift>> = _shifts

    override suspend fun addShift(shift: WorkShift): Result<WorkShift> {
        _shifts.value = _shifts.value + shift
        return Result.success(shift)
    }

    override suspend fun removeShift(id: String) {
        _shifts.value = _shifts.value.filterNot { it.id == id }
    }
}
