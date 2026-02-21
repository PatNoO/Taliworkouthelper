package com.example.taliworkouthelper.template

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

private val ScreenTextColor = Color(0xFFF5F2FF)
private val ScreenMutedText = Color(0xFFEADBFF)
private val ScreenBackgroundTop = Color(0xFF070710)
private val ScreenBackgroundBottom = Color(0xFF151233)
private val HeaderBackground = Color(0xDB14122C)
private val HeaderBorder = Color(0x3DB48CFF)
private val InputBorder = Color(0x38B48CFF)
private val InputBackground = Color(0x08FFFFFF)
private val ModuleBackground = Color(0xDB15122E)
private val ModuleBorder = Color(0x33B48CFF)
private val ChipBackground = Color(0x387C3CFF)
private val ChipBorder = Color(0x4DB48CFF)
private val SaveStart = Color(0xFF7C3CFF)
private val SaveEnd = Color(0xFFD54FFF)

@Composable
fun WorkoutTemplateScreen(
    state: WorkoutTemplateUiState,
    onTitleChanged: (String) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onExerciseSetsChanged: (String) -> Unit,
    onExerciseRepsChanged: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onSaveTemplate: () -> Unit,
    onEditTemplate: (WorkoutTemplate) -> Unit,
    onCancelEdit: () -> Unit,
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
            TemplateHeader(state = state)

            if (state.errorMessage != null) {
                ErrorStateCard(message = state.errorMessage, onDismissError = onDismissError)
            }

            WorkoutTemplateFormSection(
                state = state,
                onTitleChanged = onTitleChanged,
                onExerciseNameChanged = onExerciseNameChanged,
                onExerciseSetsChanged = onExerciseSetsChanged,
                onExerciseRepsChanged = onExerciseRepsChanged,
                onAddExercise = onAddExercise,
                onRemoveExercise = onRemoveExercise,
                onSaveTemplate = onSaveTemplate,
                onCancelEdit = onCancelEdit
            )

            when {
                state.isLoading -> LoadingStateCard()
                state.isEmpty -> EmptyStateCard()
                else -> TemplateListSection(
                    templates = state.templates,
                    onEditTemplate = onEditTemplate
                )
            }
        }
    }
}

@Composable
private fun TemplateHeader(state: WorkoutTemplateUiState) {
    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        Text("Workout Templates", color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
        Text(
            text = if (state.form.isEditing) {
                "Edit mode active: update module values and save."
            } else {
                "Builder flow: title, exercise module, then save reusable template."
            },
            color = ScreenMutedText
        )
    }
}

@Composable
private fun WorkoutTemplateFormSection(
    state: WorkoutTemplateUiState,
    onTitleChanged: (String) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onExerciseSetsChanged: (String) -> Unit,
    onExerciseRepsChanged: (String) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onSaveTemplate: () -> Unit,
    onCancelEdit: () -> Unit
) {
    val saveButtonLabel = if (state.form.isEditing) "Update template" else "Save template"

    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        TemplateInput(
            value = state.form.titleInput,
            onValueChange = onTitleChanged,
            label = "Template title"
        )

        StyledPanel(background = ModuleBackground, border = ModuleBorder) {
            Text("Exercise builder", color = ScreenTextColor, fontWeight = FontWeight.Medium)
            TemplateInput(
                value = state.form.exerciseForm.nameInput,
                onValueChange = onExerciseNameChanged,
                label = "Exercise name"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TemplateInput(
                    value = state.form.exerciseForm.setsInput,
                    onValueChange = onExerciseSetsChanged,
                    label = "Sets",
                    modifier = Modifier.weight(1f)
                )
                TemplateInput(
                    value = state.form.exerciseForm.repsInput,
                    onValueChange = onExerciseRepsChanged,
                    label = "Reps",
                    modifier = Modifier.weight(1f)
                )
            }
            ChipAction(
                text = "Add exercise",
                onClick = onAddExercise,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        if (state.form.exercises.isEmpty()) {
            Text("No exercises added", color = ScreenMutedText)
        } else {
            state.form.exercises.forEachIndexed { index, exercise ->
                StyledPanel(background = ModuleBackground, border = ModuleBorder) {
                    Text("${exercise.name} • ${exercise.sets} x ${exercise.reps}", color = ScreenTextColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ChipAction(
                            text = "Remove",
                            onClick = { onRemoveExercise(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        GradientButton(
            text = if (state.isSaving) "Saving..." else saveButtonLabel,
            onClick = onSaveTemplate,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
            gradient = Brush.horizontalGradient(listOf(SaveStart, SaveEnd))
        )

        if (state.form.isEditing) {
            ChipAction(
                text = "Cancel edit",
                onClick = onCancelEdit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TemplateListSection(
    templates: List<WorkoutTemplate>,
    onEditTemplate: (WorkoutTemplate) -> Unit
) {
    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        Text("Saved templates", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        templates.forEach { template ->
            StyledPanel(background = ModuleBackground, border = ModuleBorder) {
                Text(template.title, color = ScreenTextColor, fontWeight = FontWeight.SemiBold)
                template.exercises.forEach { exercise ->
                    Text("• ${exercise.name}: ${exercise.sets} sets x ${exercise.reps} reps", color = ScreenTextColor)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ChipAction(
                        text = "Edit",
                        onClick = { onEditTemplate(template) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingStateCard() {
    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        Text("Loading templates...", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(
            "Design note: keep builder modules visible with placeholder tone to preserve structure while loading.",
            color = ScreenMutedText
        )
    }
}

@Composable
private fun EmptyStateCard() {
    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        Text("No templates yet", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(
            "Design note: emphasize title and first exercise inputs for first-template onboarding.",
            color = ScreenMutedText
        )
    }
}

@Composable
private fun ErrorStateCard(message: String, onDismissError: () -> Unit) {
    StyledPanel(background = HeaderBackground, border = HeaderBorder) {
        Text("Error", color = ScreenTextColor, fontWeight = FontWeight.Medium)
        Text(message, color = ScreenTextColor)
        Text(
            "Design note: keep errors inline and non-blocking so builder context is not lost.",
            color = ScreenMutedText
        )
        ChipAction(
            text = "Dismiss",
            onClick = onDismissError,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TemplateInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = ScreenMutedText) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InputBorder,
            unfocusedBorderColor = InputBorder,
            focusedContainerColor = InputBackground,
            unfocusedContainerColor = InputBackground,
            focusedTextColor = ScreenTextColor,
            unfocusedTextColor = ScreenTextColor,
            focusedLabelColor = ScreenTextColor,
            unfocusedLabelColor = ScreenMutedText
        )
    )
}

@Composable
private fun ChipAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ChipBackground),
        border = BorderStroke(1.dp, ChipBorder),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ScreenMutedText
        )
    ) {
        Text(text)
    }
}

@Composable
private fun GradientButton(
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
            .background(gradient),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent
        )
    ) {
        Text(text)
    }
}

@Composable
private fun StyledPanel(
    background: Color,
    border: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, border),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
