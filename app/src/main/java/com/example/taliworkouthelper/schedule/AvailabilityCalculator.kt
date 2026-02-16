package com.example.taliworkouthelper.schedule

import java.time.DayOfWeek
import kotlin.math.max

enum class AvailabilityScope {
    DAY,
    WEEK
}

data class AvailabilitySlot(
    val dayOfWeek: DayOfWeek,
    val startHour: Int,
    val endHour: Int
) {
    val durationMinutes: Int = (endHour - startHour) * 60
}

object AvailabilityCalculator {
    private const val START_OF_DAY_HOUR = 6
    private const val END_OF_DAY_HOUR = 22

    fun calculate(
        shifts: List<WorkShift>,
        scope: AvailabilityScope,
        selectedDay: DayOfWeek,
        minDurationMinutes: Int
    ): List<AvailabilitySlot> {
        val days = if (scope == AvailabilityScope.DAY) listOf(selectedDay) else DayOfWeek.entries
        return days.flatMap { day ->
            freeSlotsForDay(
                shifts = shifts.filter { it.dayOfWeek == day },
                dayOfWeek = day
            )
        }.filter { it.durationMinutes >= minDurationMinutes }
    }

    private fun freeSlotsForDay(shifts: List<WorkShift>, dayOfWeek: DayOfWeek): List<AvailabilitySlot> {
        if (shifts.isEmpty()) {
            return listOf(AvailabilitySlot(dayOfWeek, START_OF_DAY_HOUR, END_OF_DAY_HOUR))
        }

        val sorted = shifts.sortedBy { it.startHour }
        val slots = mutableListOf<AvailabilitySlot>()
        var cursor = START_OF_DAY_HOUR

        for (shift in sorted) {
            val shiftStart = shift.startHour.coerceIn(START_OF_DAY_HOUR, END_OF_DAY_HOUR)
            val shiftEnd = shift.endHour.coerceIn(START_OF_DAY_HOUR, END_OF_DAY_HOUR)

            if (shiftStart > cursor) {
                slots += AvailabilitySlot(dayOfWeek, cursor, shiftStart)
            }
            cursor = max(cursor, shiftEnd)
        }

        if (cursor < END_OF_DAY_HOUR) {
            slots += AvailabilitySlot(dayOfWeek, cursor, END_OF_DAY_HOUR)
        }

        return slots
    }
}
