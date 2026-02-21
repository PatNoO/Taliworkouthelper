package com.example.taliworkouthelper.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Workout Templates")

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
            state.isLoading -> Text("Loading templates...")
            state.isEmpty -> Text("No templates yet")
            else -> TemplateListSection(
                templates = state.templates,
                onEditTemplate = onEditTemplate
            )
        }
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

    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.form.titleInput,
                onValueChange = onTitleChanged,
                label = { Text("Template title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Add exercise")
            OutlinedTextField(
                value = state.form.exerciseForm.nameInput,
                onValueChange = onExerciseNameChanged,
                label = { Text("Exercise name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.form.exerciseForm.setsInput,
                    onValueChange = onExerciseSetsChanged,
                    label = { Text("Sets") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.form.exerciseForm.repsInput,
                    onValueChange = onExerciseRepsChanged,
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Button(onClick = onAddExercise) {
                Text("Add exercise")
            }

            if (state.form.exercises.isEmpty()) {
                Text("No exercises added")
            } else {
                state.form.exercises.forEachIndexed { index, exercise ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${exercise.name} • ${exercise.sets}x${exercise.reps}")
                        Button(onClick = { onRemoveExercise(index) }) {
                            Text("Remove")
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSaveTemplate, enabled = !state.isSaving) {
                    Text(if (state.isSaving) "Saving..." else saveButtonLabel)
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
private fun TemplateListSection(
    templates: List<WorkoutTemplate>,
    onEditTemplate: (WorkoutTemplate) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(templates, key = { _, template -> template.id }) { _, template ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(template.title)
                    template.exercises.forEach { exercise ->
                        Text("- ${exercise.name}: ${exercise.sets} sets x ${exercise.reps} reps")
                    }
                    Button(onClick = { onEditTemplate(template) }) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}
