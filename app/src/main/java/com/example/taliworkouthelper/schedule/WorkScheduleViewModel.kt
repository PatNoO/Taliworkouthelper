package com.example.taliworkouthelper.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class WorkScheduleState(val shifts: List<WorkShift> = emptyList())

class WorkScheduleViewModel(private val repo: WorkScheduleRepository) : ViewModel() {
    val state: StateFlow<WorkScheduleState> = repo.getShifts()
        .map { list -> WorkScheduleState(shifts = list) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WorkScheduleState())

    fun addShift(shift: WorkShift, onResult: (Result<WorkShift>) -> Unit) {
        viewModelScope.launch {
            val res = repo.addShift(shift)
            onResult(res)
        }
    }

    fun removeShift(id: String) {
        viewModelScope.launch { repo.removeShift(id) }
    }
}
