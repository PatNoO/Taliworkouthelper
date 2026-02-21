package com.example.taliworkouthelper.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun WorkScheduleScreen(
    state: WorkScheduleUiState,
    onStartHourChanged: (String) -> Unit,
    onEndHourChanged: (String) -> Unit,
    onFormDayChange: (DayOfWeek) -> Unit,
    onSubmitShift: () -> Unit,
    onCancelEdit: () -> Unit,
    onEditShift: (WorkShift) -> Unit,
    onDeleteShift: (String) -> Unit,
    onScopeChange: (AvailabilityScope) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDayChange: (DayOfWeek) -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Work Schedule")

        if (state.errorMessage != null) {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Error: ${state.errorMessage}")
                    Button(onClick = onDismissError) {
                        Text("Dismiss")
                    }
                }
            }
        }

        ShiftFormSection(
            state = state,
            onStartHourChanged = onStartHourChanged,
            onEndHourChanged = onEndHourChanged,
            onFormDayChange = onFormDayChange,
            onSubmitShift = onSubmitShift,
            onCancelEdit = onCancelEdit
        )

        AvailabilitySection(
            state = state,
            onScopeChange = onScopeChange,
            onDurationChange = onDurationChange,
            onDayChange = onDayChange
        )

        Text("Work shifts")
        when {
            state.isLoading -> Text("Loading shifts...")
            state.isEmpty -> Text("No shifts yet")
            else -> ShiftListSection(
                shifts = state.shifts,
                onEditShift = onEditShift,
                onDeleteShift = onDeleteShift
            )
        }
    }
}

@Composable
private fun ShiftFormSection(
    state: WorkScheduleUiState,
    onStartHourChanged: (String) -> Unit,
    onEndHourChanged: (String) -> Unit,
    onFormDayChange: (DayOfWeek) -> Unit,
    onSubmitShift: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val saveLabel = if (state.form.isEditing) "Update shift" else "Add shift"

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shift day: ${state.form.selectedDay.label()}")
            DayFilterRow(selectedDay = state.form.selectedDay, onDayChange = onFormDayChange)
            OutlinedTextField(
                value = state.form.startHourInput,
                onValueChange = onStartHourChanged,
                label = { Text("Start hour (0-23)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.form.endHourInput,
                onValueChange = onEndHourChanged,
                label = { Text("End hour (0-23)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSubmitShift, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Saving..." else saveLabel)
                }
                if (state.form.isEditing) {
                    Button(onClick = onCancelEdit, enabled = !state.isSaving) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilitySection(
    state: WorkScheduleUiState,
    onScopeChange: (AvailabilityScope) -> Unit,
    onDurationChange: (Int) -> Unit,
    onDayChange: (DayOfWeek) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        }
    }
}

@Composable
private fun ShiftListSection(
    shifts: List<WorkShift>,
    onEditShift: (WorkShift) -> Unit,
    onDeleteShift: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        shifts.forEach { shift ->
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${shift.dayOfWeek.label()}: ${shift.startHour}:00-${shift.endHour}:00")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onEditShift(shift) }) {
                            Text("Edit")
                        }
                        Button(onClick = { onDeleteShift(shift.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
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
