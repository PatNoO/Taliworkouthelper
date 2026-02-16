package com.example.taliworkouthelper.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun WorkScheduleScreen(
    state: WorkScheduleState,
    onAddSampleShift: () -> Unit,
    onRemoveShift: (String) -> Unit,
    onScopeChange: (AvailabilityScope) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDayChange: (DayOfWeek) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Availability")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onScopeChange(AvailabilityScope.DAY) }) { Text("Day") }
            Button(onClick = { onScopeChange(AvailabilityScope.WEEK) }) { Text("Week") }
        }

        if (state.availabilityScope == AvailabilityScope.DAY) {
            DayFilterRow(selectedDay = state.selectedDay, onDayChange = onDayChange)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onDurationChange(45) }) { Text("45 min") }
            Button(onClick = { onDurationChange(60) }) { Text("60 min") }
            Button(onClick = { onDurationChange(90) }) { Text("90 min") }
        }

        Text("Available slots")
        if (state.availableSlots.isEmpty()) {
            Text("No available slots for selected filters.")
        } else {
            state.availableSlots.forEach { slot ->
                Text(formatSlot(slot))
            }
        }

        Text("Work shifts")
        if (state.shifts.isEmpty()) {
            Text("No work shifts yet")
        } else {
            state.shifts.forEach { shift ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${shift.dayOfWeek.label()}: ${shift.startHour}:00-${shift.endHour}:00")
                    Button(onClick = { onRemoveShift(shift.id) }) {
                        Text("Remove")
                    }
                }
            }
        }

        Button(onClick = onAddSampleShift) {
            Text("Add sample shift")
        }
    }
}

@Composable
private fun DayFilterRow(selectedDay: DayOfWeek, onDayChange: (DayOfWeek) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayFilterButton(day = DayOfWeek.MONDAY, selectedDay = selectedDay, onDayChange = onDayChange)
            DayFilterButton(day = DayOfWeek.TUESDAY, selectedDay = selectedDay, onDayChange = onDayChange)
            DayFilterButton(day = DayOfWeek.WEDNESDAY, selectedDay = selectedDay, onDayChange = onDayChange)
            DayFilterButton(day = DayOfWeek.THURSDAY, selectedDay = selectedDay, onDayChange = onDayChange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DayFilterButton(day = DayOfWeek.FRIDAY, selectedDay = selectedDay, onDayChange = onDayChange)
            DayFilterButton(day = DayOfWeek.SATURDAY, selectedDay = selectedDay, onDayChange = onDayChange)
            DayFilterButton(day = DayOfWeek.SUNDAY, selectedDay = selectedDay, onDayChange = onDayChange)
        }
    }
}

@Composable
private fun DayFilterButton(day: DayOfWeek, selectedDay: DayOfWeek, onDayChange: (DayOfWeek) -> Unit) {
    val label = if (selectedDay == day) "*${day.label()}" else day.label()
    Button(onClick = { onDayChange(day) }) {
        Text(label)
    }
}

private fun formatSlot(slot: AvailabilitySlot): String {
    return "${slot.dayOfWeek.label()}: ${slot.startHour}:00-${slot.endHour}:00 (${slot.durationMinutes} min)"
}

private fun DayOfWeek.label(): String {
    return name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
}
