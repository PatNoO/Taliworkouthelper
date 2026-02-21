package com.example.taliworkouthelper.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taliworkouthelper.session.WorkoutSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OverviewScreen(
    state: OverviewUiState,
    onOpenSessionDetail: (String) -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Overview")

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

        when {
            state.isLoading -> Text("Loading overview...")
            else -> {
                UpcomingBookingsSection(state.upcomingBookings, state.isEmptyBookings)
                WorkoutHistorySection(state.workoutHistory, state.isEmptyWorkoutHistory, onOpenSessionDetail)
            }
        }
    }
}

@Composable
fun WorkoutSessionDetailScreen(session: WorkoutSession?, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Workout Session Detail")
        Button(onClick = onBack) { Text("Back") }

        if (session == null) {
            Text("Session not found")
            return
        }

        Text("Session ID: ${session.id}")
        Text("General note: ${session.generalNote.ifBlank { "No note" }}")

        if (session.exerciseLogs.isEmpty()) {
            Text("No logged exercises")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(session.exerciseLogs) { log ->
                    Card {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(log.exerciseName)
                            if (log.note.isNotBlank()) {
                                Text("Note: ${log.note}")
                            }
                            log.sets.forEachIndexed { index, set ->
                                Text("Set ${index + 1}: ${set.reps} reps @ ${set.weightKg ?: 0.0} kg")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingBookingsSection(bookings: List<UpcomingBooking>, isEmpty: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Upcoming bookings")
        if (isEmpty) {
            Text("No upcoming bookings")
            return
        }

        bookings.forEach { booking ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(booking.partnerName)
                    if (booking.title.isNotBlank()) {
                        Text(booking.title)
                    }
                    Text(formatBookingTime(booking.startAtMillis, booking.endAtMillis))
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistorySection(
    history: List<WorkoutSession>,
    isEmpty: Boolean,
    onOpenSessionDetail: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Workout history")
        if (isEmpty) {
            Text("No completed workouts")
            return
        }

        history.forEach { session ->
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Session ${session.id.take(8)}")
                        Text("Exercises: ${session.exerciseLogs.size}")
                    }
                    Button(onClick = { onOpenSessionDetail(session.id) }) {
                        Text("Details")
                    }
                }
            }
        }
    }
}

private fun formatBookingTime(startMillis: Long, endMillis: Long?): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d HH:mm")
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(startMillis).atZone(zone).format(formatter)
    val end = endMillis?.let { Instant.ofEpochMilli(it).atZone(zone).format(formatter) }
    return if (end != null) "$start - $end" else start
}
