package com.example.taliworkouthelper.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkScheduleState(
    val shifts: List<WorkShift> = emptyList(),
    val availabilityScope: AvailabilityScope = AvailabilityScope.DAY,
    val selectedDay: DayOfWeek = DayOfWeek.MONDAY,
    val minDurationMinutes: Int = 45,
    val availableSlots: List<AvailabilitySlot> = emptyList()
)

class WorkScheduleViewModel(private val repo: WorkScheduleRepository) : ViewModel() {
    private val scopeFlow = MutableStateFlow(AvailabilityScope.DAY)
    private val dayFlow = MutableStateFlow(DayOfWeek.MONDAY)
    private val durationFlow = MutableStateFlow(45)

    val state: StateFlow<WorkScheduleState> = combine(
        repo.getShifts(),
        scopeFlow,
        dayFlow,
        durationFlow
    ) { shifts, scope, day, minDuration ->
        WorkScheduleState(
            shifts = shifts,
            availabilityScope = scope,
            selectedDay = day,
            minDurationMinutes = minDuration,
            availableSlots = AvailabilityCalculator.calculate(
                shifts = shifts,
                scope = scope,
                selectedDay = day,
                minDurationMinutes = minDuration
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WorkScheduleState())

    fun addShift(shift: WorkShift, onResult: (Result<WorkShift>) -> Unit) {
        viewModelScope.launch {
            val res = repo.addShift(shift)
            onResult(res)
        }
    }

    fun removeShift(id: String) {
        viewModelScope.launch { repo.removeShift(id) }
    }

    fun setAvailabilityScope(scope: AvailabilityScope) {
        scopeFlow.value = scope
    }

    fun setSelectedDay(day: DayOfWeek) {
        dayFlow.value = day
    }

    fun setMinDurationMinutes(minutes: Int) {
        durationFlow.value = minutes
    }
}
