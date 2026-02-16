package com.example.taliworkouthelper.schedule

import java.time.DayOfWeek

data class WorkShift(
    val id: String,
    val startHour: Int,
    val endHour: Int,
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY
)
