package com.example.taliworkouthelper.overview

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taliworkouthelper.session.WorkoutSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DetailTextColor = Color(0xFFF5F1FF)
private val DetailMetaColor = Color(0xFFC8BCE8)
private val DetailBackgroundTop = Color(0xFF070711)
private val DetailBackgroundBottom = Color(0xFF121029)
private val DetailCardBackground = Color(0xDB15122E)
private val DetailCardBorder = Color(0x38B48CFF)
private val BackButtonBackground = Color(0x0AFFFFFF)
private val BackButtonBorder = Color(0x4DB48CFF)

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DetailBackgroundTop, DetailBackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailTopBar(onBack = onBack)

            if (session == null) {
                DetailCard {
                    Text("Session not found", color = DetailTextColor, fontWeight = FontWeight.Medium)
                    Text(
                        "Fallback display keeps back navigation visible when detail data is missing.",
                        color = DetailMetaColor
                    )
                }
                return@Column
            }

            DetailCard {
                Text("Session detail", color = DetailTextColor, fontWeight = FontWeight.SemiBold)
                Text("Session ID: ${session.id}", color = DetailMetaColor)
                Text(
                    "General note: ${session.generalNote.ifBlank { "No note" }}",
                    color = DetailTextColor
                )
            }

            if (session.exerciseLogs.isEmpty()) {
                DetailCard {
                    Text("No logged exercises", color = DetailTextColor, fontWeight = FontWeight.Medium)
                    Text(
                        "Empty exercise list fallback is explicitly defined for readability.",
                        color = DetailMetaColor
                    )
                }
            } else {
                session.exerciseLogs.forEach { log ->
                    DetailCard {
                        Text(log.exerciseName, color = DetailTextColor, fontWeight = FontWeight.SemiBold)
                        if (log.note.isNotBlank()) {
                            Text("Note: ${log.note}", color = DetailMetaColor)
                        }
                        log.sets.forEachIndexed { index, set ->
                            SetRow(
                                setLabel = "Set ${index + 1}",
                                reps = "${set.reps} reps",
                                weight = "${set.weightKg ?: 0.0} kg"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(BackButtonBackground),
            border = BorderStroke(1.dp, BackButtonBorder),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = DetailTextColor
            )
        ) {
            Text("Back")
        }
        Text("Workout Session Detail", color = DetailTextColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DetailCardBackground),
        border = BorderStroke(1.dp, DetailCardBorder),
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

@Composable
private fun SetRow(setLabel: String, reps: String, weight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(setLabel, color = DetailTextColor, modifier = Modifier.weight(1f))
        Text(reps, color = DetailMetaColor)
        Text(weight, color = DetailMetaColor)
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
