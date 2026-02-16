package com.example.taliworkouthelper.schedule

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityCalculatorTest {
    @Test
    fun `day scope returns free slots around shifts`() {
        val shifts = listOf(
            WorkShift(id = "m1", startHour = 9, endHour = 12, dayOfWeek = DayOfWeek.MONDAY),
            WorkShift(id = "m2", startHour = 14, endHour = 17, dayOfWeek = DayOfWeek.MONDAY)
        )

        val slots = AvailabilityCalculator.calculate(
            shifts = shifts,
            scope = AvailabilityScope.DAY,
            selectedDay = DayOfWeek.MONDAY,
            minDurationMinutes = 45
        )

        assertEquals(3, slots.size)
        assertEquals(6, slots[0].startHour)
        assertEquals(9, slots[0].endHour)
        assertEquals(12, slots[1].startHour)
        assertEquals(14, slots[1].endHour)
        assertEquals(17, slots[2].startHour)
        assertEquals(22, slots[2].endHour)
    }

    @Test
    fun `duration filter removes short slots`() {
        val shifts = listOf(
            WorkShift(id = "m1", startHour = 6, endHour = 7, dayOfWeek = DayOfWeek.MONDAY),
            WorkShift(id = "m2", startHour = 8, endHour = 22, dayOfWeek = DayOfWeek.MONDAY)
        )

        val slots = AvailabilityCalculator.calculate(
            shifts = shifts,
            scope = AvailabilityScope.DAY,
            selectedDay = DayOfWeek.MONDAY,
            minDurationMinutes = 90
        )

        assertTrue(slots.isEmpty())
    }

    @Test
    fun `week scope includes all days`() {
        val shifts = listOf(
            WorkShift(id = "m", startHour = 9, endHour = 17, dayOfWeek = DayOfWeek.MONDAY)
        )

        val slots = AvailabilityCalculator.calculate(
            shifts = shifts,
            scope = AvailabilityScope.WEEK,
            selectedDay = DayOfWeek.MONDAY,
            minDurationMinutes = 45
        )

        assertTrue(slots.any { it.dayOfWeek == DayOfWeek.TUESDAY && it.startHour == 6 && it.endHour == 22 })
    }
}
