package com.example.taliworkouthelper.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun ExerciseLibraryScreen(
    state: ExerciseLibraryUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSelectMuscleGroup: (String) -> Unit,
    onOpenExerciseDetail: (String) -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Exercise library")

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

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Search exercises") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.muscleGroupFilters) { group ->
                Button(onClick = { onSelectMuscleGroup(group) }) {
                    val marker = if (group == state.selectedMuscleGroup) "* " else ""
                    Text("$marker$group")
                }
            }
        }

        when {
            state.isLoading -> Text("Loading exercises...")
            state.isEmpty -> Text("No exercises match your filters")
            else -> ExerciseList(
                exercises = state.filteredExercises,
                onOpenExerciseDetail = onOpenExerciseDetail
            )
        }
    }
}

@Composable
fun ExerciseDetailScreen(exercise: Exercise?, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Exercise detail")
        Button(onClick = onBack) { Text("Back") }

        if (exercise == null) {
            Text("Exercise not found")
            return
        }

        ExerciseImage(
            imageUrl = exercise.imageUrl,
            contentDescription = "${exercise.name} image"
        )
        Text(exercise.name)
        Text("Muscle group: ${exercise.muscleGroup}")
        Text("Equipment: ${exercise.equipment}")
        Text(exercise.description)
    }
}

@Composable
private fun ExerciseList(
    exercises: List<Exercise>,
    onOpenExerciseDetail: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(exercises, key = { it.id }) { exercise ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseImage(
                        imageUrl = exercise.imageUrl,
                        contentDescription = "${exercise.name} image"
                    )
                    Text(exercise.name)
                    Text("${exercise.muscleGroup} • ${exercise.equipment}")
                    Button(onClick = { onOpenExerciseDetail(exercise.id) }) {
                        Text("View details")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseImage(
    imageUrl: String,
    contentDescription: String
) {
    if (imageUrl.isBlank()) {
        ImageFallback()
        return
    }

    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading image...")
            }
        },
        error = {
            ImageFallback()
        }
    )
}

@Composable
private fun ImageFallback() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFE8E8E8)),
        contentAlignment = Alignment.Center
    ) {
        Text("Image unavailable")
    }
}
