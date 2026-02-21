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

@Composable
fun WorkScheduleScreen(
    state: WorkScheduleUiState,
    onStartHourChanged: (String) -> Unit,
    onEndHourChanged: (String) -> Unit,
    onSubmitShift: () -> Unit,
    onCancelEdit: () -> Unit,
    onEditShift: (WorkShift) -> Unit,
    onDeleteShift: (String) -> Unit,
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
            onSubmitShift = onSubmitShift,
            onCancelEdit = onCancelEdit
        )

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
    onSubmitShift: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val saveLabel = if (state.form.isEditing) "Update shift" else "Add shift"

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text("${shift.startHour}:00 - ${shift.endHour}:00")
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
