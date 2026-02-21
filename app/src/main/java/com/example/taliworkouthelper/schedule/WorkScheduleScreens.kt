package com.example.taliworkouthelper.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.time.DayOfWeek

private val ScreenTextColor = Color(0xFFF5F2FF)
private val ScreenMutedTextColor = Color(0xFFB7A8D9)
private val ScreenBackgroundTop = Color(0xFF070710)
private val ScreenBackgroundBottom = Color(0xFF151232)
private val DayTabBorderColor = Color(0x38B48CFF)
private val DayTabBackgroundColor = Color(0x08FFFFFF)
private val DayTabActiveStart = Color(0x997C3CFF)
private val DayTabActiveEnd = Color(0x80D54FFF)
private val CardBackgroundColor = Color(0xDB14122C)
private val CardBorderColor = Color(0x3DB48CFF)
private val InputBackgroundColor = Color(0x08FFFFFF)
private val InputBorderColor = Color(0x38B48CFF)

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScreenBackgroundTop, ScreenBackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Work Schedule", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)

            if (state.errorMessage != null) {
                ErrorStateCard(message = state.errorMessage, onDismissError = onDismissError)
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

            Text("Work shifts", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
            when {
                state.isLoading -> LoadingStateCard()
                state.isEmpty -> EmptyStateCard()
                else -> ShiftListSection(
                    shifts = state.shifts,
                    onEditShift = onEditShift,
                    onDeleteShift = onDeleteShift
                )
            }
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

    StyledPanel {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Shift form", color = ScreenTextColor, fontWeight = FontWeight.Medium)
            DayTabs(selectedDay = state.form.selectedDay, onDayChange = onFormDayChange)
            ScheduleInput(
                value = state.form.startHourInput,
                onValueChange = onStartHourChanged,
                label = "Start hour (0-23)"
            )
            ScheduleInput(
                value = state.form.endHourInput,
                onValueChange = onEndHourChanged,
                label = "End hour (0-23)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GradientButton(
                    text = if (state.isSaving) "Saving..." else saveLabel,
                    onClick = onSubmitShift,
                    enabled = !state.isSaving,
                    gradient = Brush.horizontalGradient(listOf(DayTabActiveStart, DayTabActiveEnd)),
                    modifier = Modifier.weight(1f)
                )
                if (state.form.isEditing) {
                    GradientButton(
                        text = "Cancel",
                        onClick = onCancelEdit,
                        enabled = !state.isSaving,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF5B4D8A), Color(0xFF8B6FB9))),
                        modifier = Modifier.weight(1f)
                    )
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
    StyledPanel {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Availability filters", color = ScreenTextColor, fontWeight = FontWeight.Medium)

            SegmentedOptionRow(
                title = "Scope",
                options = listOf(
                    OptionItem(
                        label = "Day",
                        selected = state.availabilityScope == AvailabilityScope.DAY,
                        onClick = { onScopeChange(AvailabilityScope.DAY) }
                    ),
                    OptionItem(
                        label = "Week",
                        selected = state.availabilityScope == AvailabilityScope.WEEK,
                        onClick = { onScopeChange(AvailabilityScope.WEEK) }
                    )
                )
            )

            if (state.availabilityScope == AvailabilityScope.DAY) {
                DayTabs(selectedDay = state.selectedDay, onDayChange = onDayChange)
            }

            SegmentedOptionRow(
                title = "Duration",
                options = listOf(
                    OptionItem("45 min", state.minDurationMinutes == 45) { onDurationChange(45) },
                    OptionItem("60 min", state.minDurationMinutes == 60) { onDurationChange(60) },
                    OptionItem("90 min", state.minDurationMinutes == 90) { onDurationChange(90) }
                )
            )

            Text("Available slots", color = ScreenTextColor, fontWeight = FontWeight.Medium)
            if (state.availableSlots.isEmpty()) {
                Text("No available slots for selected filters.", color = ScreenMutedTextColor)
                Text(
                    "Design note: keep filters visible so users can quickly adjust criteria.",
                    color = ScreenMutedTextColor
                )
            } else {
                state.availableSlots.forEach { slot ->
                    Text(formatSlot(slot), color = ScreenTextColor)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        shifts.forEach { shift ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
                border = BorderStroke(1.dp, CardBorderColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${shift.dayOfWeek.label()} ${shift.startHour}:00-${shift.endHour}:00",
                        color = ScreenTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shift card with compact action row for fast schedule edits",
                        color = ScreenMutedTextColor
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        GradientButton(
                            text = "Edit",
                            onClick = { onEditShift(shift) },
                            enabled = true,
                            gradient = Brush.horizontalGradient(listOf(DayTabActiveStart, DayTabActiveEnd)),
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "Delete",
                            onClick = { onDeleteShift(shift.id) },
                            enabled = true,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF823A9E), Color(0xFFB2509E))),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayTabs(selectedDay: DayOfWeek, onDayChange: (DayOfWeek) -> Unit) {
    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            val selected = day == selectedDay
            Button(
                onClick = { onDayChange(day) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (selected) Color.Transparent else DayTabBorderColor),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = ScreenTextColor
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(listOf(DayTabActiveStart, DayTabActiveEnd))
                            } else {
                                Brush.horizontalGradient(listOf(DayTabBackgroundColor, DayTabBackgroundColor))
                            }
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day.label(), color = ScreenTextColor)
                }
            }
        }
    }
}

@Composable
private fun SegmentedOptionRow(title: String, options: List<OptionItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = ScreenMutedTextColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                GradientButton(
                    text = option.label,
                    onClick = option.onClick,
                    enabled = true,
                    gradient = if (option.selected) {
                        Brush.horizontalGradient(listOf(DayTabActiveStart, DayTabActiveEnd))
                    } else {
                        Brush.horizontalGradient(listOf(InputBackgroundColor, InputBackgroundColor))
                    },
                    modifier = Modifier.weight(1f),
                    contentColor = ScreenTextColor
                )
            }
        }
    }
}

@Composable
private fun LoadingStateCard() {
    StyledPanel {
        Text("Loading shifts...", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(
            "Design note: show skeleton or placeholder cards to keep layout stable while loading.",
            color = ScreenMutedTextColor
        )
    }
}

@Composable
private fun EmptyStateCard() {
    StyledPanel {
        Text("No shifts yet", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(
            "Design note: keep shift form prominent so first shift can be added immediately.",
            color = ScreenMutedTextColor
        )
    }
}

@Composable
private fun ErrorStateCard(message: String, onDismissError: () -> Unit) {
    StyledPanel {
        Text("Error", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(message, color = ScreenTextColor)
        Text(
            "Design note: provide inline recovery without hiding scheduler controls.",
            color = ScreenMutedTextColor
        )
        GradientButton(
            text = "Dismiss",
            onClick = onDismissError,
            enabled = true,
            gradient = Brush.horizontalGradient(listOf(DayTabActiveStart, DayTabActiveEnd)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ScheduleInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ScreenMutedTextColor) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputBorderColor,
            unfocusedBorderColor = InputBorderColor,
            focusedContainerColor = InputBackgroundColor,
            unfocusedContainerColor = InputBackgroundColor,
            focusedTextColor = ScreenTextColor,
            unfocusedTextColor = ScreenTextColor,
            focusedLabelColor = ScreenTextColor,
            unfocusedLabelColor = ScreenMutedTextColor
        )
    )
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    gradient: Brush,
    modifier: Modifier = Modifier,
    contentColor: Color = ScreenTextColor
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(gradient),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent
        )
    ) {
        Text(text)
    }
}

@Composable
private fun StyledPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        border = BorderStroke(1.dp, CardBorderColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

private data class OptionItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

private fun formatSlot(slot: AvailabilitySlot): String {
    return "${slot.dayOfWeek.label()}: ${slot.startHour}:00-${slot.endHour}:00 (${slot.durationMinutes} min)"
}

private fun DayOfWeek.label(): String {
    return name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
}
