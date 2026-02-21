package com.example.taliworkouthelper.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    onExerciseNameChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onExerciseNoteChanged: (String) -> Unit,
    onGeneralNoteChanged: (String) -> Unit,
    onAddSet: () -> Unit,
    onCompleteSession: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Active Workout")

        if (state.errorMessage != null) {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Error: ${state.errorMessage}")
                    Button(onClick = onDismissError) { Text("Dismiss") }
                }
            }
        }

        when {
            state.isLoading -> Text("Loading session...")
            else -> {
                ActiveEntryForm(
                    state = state,
                    onExerciseNameChanged = onExerciseNameChanged,
                    onRepsChanged = onRepsChanged,
                    onWeightChanged = onWeightChanged,
                    onExerciseNoteChanged = onExerciseNoteChanged,
                    onGeneralNoteChanged = onGeneralNoteChanged,
                    onAddSet = onAddSet,
                    onCompleteSession = onCompleteSession
                )

                ActiveLogSection(state.activeSession)
                HistorySection(state.history)
            }
        }
    }
}

@Composable
private fun ActiveEntryForm(
    state: ActiveWorkoutUiState,
    onExerciseNameChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onExerciseNoteChanged: (String) -> Unit,
    onGeneralNoteChanged: (String) -> Unit,
    onAddSet: () -> Unit,
    onCompleteSession: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Log set")
            OutlinedTextField(
                value = state.setEntryForm.exerciseNameInput,
                onValueChange = onExerciseNameChanged,
                label = { Text("Exercise name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.setEntryForm.repsInput,
                    onValueChange = onRepsChanged,
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.setEntryForm.weightInput,
                    onValueChange = onWeightChanged,
                    label = { Text("Weight kg (optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.setEntryForm.exerciseNoteInput,
                onValueChange = onExerciseNoteChanged,
                label = { Text("Exercise note") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.activeSession?.generalNote.orEmpty(),
                onValueChange = onGeneralNoteChanged,
                label = { Text("General session note") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddSet, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Saving..." else "Add set")
                }
                Button(onClick = onCompleteSession, enabled = state.hasActiveLogs && !state.isSaving) {
                    Text("Complete session")
                }
            }
        }
    }
}

@Composable
private fun ActiveLogSection(activeSession: WorkoutSession?) {
    Text("Current session")
    if (activeSession == null || activeSession.exerciseLogs.isEmpty()) {
        Text("No sets logged yet")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        activeSession.exerciseLogs.forEach { log ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(log.exerciseName)
                    log.sets.forEachIndexed { index, set ->
                        val weightText = set.weightKg?.let { " @ ${it}kg" } ?: ""
                        Text("Set ${index + 1}: ${set.reps} reps$weightText")
                    }
                    if (log.note.isNotBlank()) {
                        Text("Note: ${log.note}")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<WorkoutSession>) {
    Text("Workout history")
    if (history.isEmpty()) {
        Text("No completed sessions")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(history, key = { it.id }) { session ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Session ${session.id.take(8)}")
                    Text("Exercises: ${session.exerciseLogs.size}")
                    if (session.generalNote.isNotBlank()) {
                        Text("Note: ${session.generalNote}")
                    }
                }
            }
        }
    }
}
