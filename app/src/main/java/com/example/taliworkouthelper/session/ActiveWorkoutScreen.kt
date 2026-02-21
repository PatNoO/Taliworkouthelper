package com.example.taliworkouthelper.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ScreenTextColor = Color(0xFFF7F3FF)
private val ScreenBackgroundTop = Color(0xFF06060F)
private val ScreenBackgroundBottom = Color(0xFF13112C)
private val ScreenAccentGlow = Color(0x477C3CFF)
private val CardBackgroundColor = Color(0xE615122E)
private val CardBorderColor = Color(0x3DB48CFF)
private val InputBorderColor = Color(0x38B48CFF)
private val InputBackgroundColor = Color(0x08FFFFFF)
private val AddButtonStart = Color(0xFF7C3CFF)
private val AddButtonEnd = Color(0xFFB046FF)
private val CompleteButtonStart = Color(0xFFD54FFF)
private val CompleteButtonEnd = Color(0xFFFF62A7)

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ScreenBackgroundTop, ScreenBackgroundBottom)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(ScreenAccentGlow, Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeaderSection(logCount = state.activeSession?.exerciseLogs?.size ?: 0)
            }

            if (state.errorMessage != null) {
                item {
                    ErrorStateCard(message = state.errorMessage, onDismissError = onDismissError)
                }
            }

            when {
                state.isLoading -> {
                    item { LoadingStateCard() }
                }

                else -> {
                    item {
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
                    }
                    item { ActiveLogSection(state.activeSession) }
                    item { HistorySection(state.history) }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(logCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Active Workout",
                    color = ScreenTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "High-focus logging flow for sets, reps, weight, and notes",
                    color = ScreenTextColor.copy(alpha = 0.82f)
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (logCount / 8f).coerceIn(0.05f, 1f) },
                    color = AddButtonEnd,
                    trackColor = ScreenTextColor.copy(alpha = 0.12f)
                )
                Text(text = "$logCount", color = ScreenTextColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LoadingStateCard() {
    StyledCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Loading session...", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            Text(
                "Design note: keep metric inputs hidden until data is ready to prevent accidental entry.",
                color = ScreenTextColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorStateCard(message: String, onDismissError: () -> Unit) {
    StyledCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Error", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            Text(message, color = ScreenTextColor)
            Text(
                "Design note: show inline recovery action before the user loses workout context.",
                color = ScreenTextColor.copy(alpha = 0.8f)
            )
            Button(
                onClick = onDismissError,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = ScreenTextColor
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AddButtonStart, AddButtonEnd)
                        )
                    )
            ) {
                Text("Dismiss")
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
    StyledCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Exercise logging", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetricInput(
                    value = state.setEntryForm.exerciseNameInput,
                    onValueChange = onExerciseNameChanged,
                    label = "Exercise",
                    modifier = Modifier.weight(1.5f)
                )
                MetricInput(
                    value = state.setEntryForm.repsInput,
                    onValueChange = onRepsChanged,
                    label = "Reps",
                    modifier = Modifier.weight(0.75f)
                )
                MetricInput(
                    value = state.setEntryForm.weightInput,
                    onValueChange = onWeightChanged,
                    label = "Weight",
                    modifier = Modifier.weight(0.75f)
                )
            }

            MetricInput(
                value = state.setEntryForm.exerciseNoteInput,
                onValueChange = onExerciseNoteChanged,
                label = "Exercise note",
                modifier = Modifier.fillMaxWidth()
            )

            MetricInput(
                value = state.activeSession?.generalNote.orEmpty(),
                onValueChange = onGeneralNoteChanged,
                label = "Session note",
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GradientActionButton(
                    text = if (state.isSaving) "Saving..." else "Add set",
                    onClick = onAddSet,
                    enabled = !state.isSaving,
                    gradient = Brush.horizontalGradient(colors = listOf(AddButtonStart, AddButtonEnd)),
                    modifier = Modifier.weight(1f)
                )
                GradientActionButton(
                    text = "Complete session",
                    onClick = onCompleteSession,
                    enabled = state.hasActiveLogs && !state.isSaving,
                    gradient = Brush.horizontalGradient(colors = listOf(CompleteButtonStart, CompleteButtonEnd)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ScreenTextColor.copy(alpha = 0.75f)) },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputBorderColor,
            unfocusedBorderColor = InputBorderColor,
            focusedContainerColor = InputBackgroundColor,
            unfocusedContainerColor = InputBackgroundColor,
            focusedTextColor = ScreenTextColor,
            unfocusedTextColor = ScreenTextColor,
            cursorColor = ScreenTextColor,
            focusedLabelColor = ScreenTextColor,
            unfocusedLabelColor = ScreenTextColor.copy(alpha = 0.75f)
        ),
        singleLine = true
    )
}

@Composable
private fun ActiveLogSection(activeSession: WorkoutSession?) {
    StyledCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Current session", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            if (activeSession == null || activeSession.exerciseLogs.isEmpty()) {
                Text("No sets logged yet", color = ScreenTextColor.copy(alpha = 0.8f))
                return@Column
            }

            activeSession.exerciseLogs.forEach { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = InputBackgroundColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(log.exerciseName, color = ScreenTextColor, fontWeight = FontWeight.Medium)
                        log.sets.forEachIndexed { index, set ->
                            val weightText = set.weightKg?.let { " @ ${it}kg" } ?: ""
                            Text("Set ${index + 1}: ${set.reps} reps$weightText", color = ScreenTextColor)
                        }
                        if (log.note.isNotBlank()) {
                            Text("Note: ${log.note}", color = ScreenTextColor.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<WorkoutSession>) {
    StyledCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Workout history", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            if (history.isEmpty()) {
                Text("No completed sessions", color = ScreenTextColor.copy(alpha = 0.8f))
                return@Column
            }

            history.forEach { session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = InputBackgroundColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Session ${session.id.take(8)}", color = ScreenTextColor)
                        Text("Exercises: ${session.exerciseLogs.size}", color = ScreenTextColor.copy(alpha = 0.9f))
                        if (session.generalNote.isNotBlank()) {
                            Text("Note: ${session.generalNote}", color = ScreenTextColor.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = ScreenTextColor,
            disabledContentColor = ScreenTextColor.copy(alpha = 0.65f)
        )
    ) {
        Text(text)
    }
}

@Composable
private fun StyledCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
