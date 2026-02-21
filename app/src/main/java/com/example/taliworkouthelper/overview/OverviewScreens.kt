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

private val OverviewTextColor = Color(0xFFF4F1FF)
private val OverviewMetaColor = Color(0xFFC5B8E6)
private val OverviewBackgroundTop = Color(0xFF080810)
private val OverviewBackgroundBottom = Color(0xFF141230)
private val OverviewCardBackground = Color(0xD914122C)
private val OverviewCardBorder = Color(0x42B78BFF)
private val OverviewItemBackground = Color(0x08FFFFFF)
private val OverviewItemBorder = Color(0x2EB78BFF)
private val OverviewTagBackground = Color(0x4D7C3CFF)
private val OverviewTagText = Color(0xFFE9DCFF)

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(OverviewBackgroundTop, OverviewBackgroundBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Overview", color = OverviewTextColor, fontWeight = FontWeight.SemiBold)

            if (state.errorMessage != null) {
                OverviewCard {
                    Text("Error", color = OverviewTextColor, fontWeight = FontWeight.Medium)
                    Text(state.errorMessage, color = OverviewTextColor)
                    Text(
                        "Design note: keep error inline so upcoming/history context remains visible.",
                        color = OverviewMetaColor
                    )
                    Button(
                        onClick = onDismissError,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = OverviewTextColor
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF7C3CFF), Color(0xFFD54FFF))))
                    ) {
                        Text("Dismiss")
                    }
                }
            }

            when {
                state.isLoading -> {
                    OverviewCard {
                        Text("Loading overview...", color = OverviewTextColor, fontWeight = FontWeight.Medium)
                        Text(
                            "Design note: preserve split sections with placeholders while data streams in.",
                            color = OverviewMetaColor
                        )
                    }
                }

                else -> {
                    UpcomingBookingsSection(state.upcomingBookings, state.isEmptyBookings)
                    WorkoutHistorySection(state.workoutHistory, state.isEmptyWorkoutHistory, onOpenSessionDetail)
                }
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
private fun UpcomingBookingsSection(bookings: List<UpcomingBooking>, isEmpty: Boolean) {
    OverviewCard {
        SectionHeader(title = "Upcoming", tag = "Next")
        if (isEmpty) {
            Text("No upcoming bookings", color = OverviewTextColor)
            Text(
                "Design note: keep this section visible to reinforce split overview layout.",
                color = OverviewMetaColor
            )
            return@OverviewCard
        }

        bookings.forEach { booking ->
            OverviewItem {
                Text(booking.partnerName, color = OverviewTextColor, fontWeight = FontWeight.Medium)
                if (booking.title.isNotBlank()) {
                    Text(booking.title, color = OverviewMetaColor)
                }
                Text(formatBookingTime(booking.startAtMillis, booking.endAtMillis), color = OverviewMetaColor)
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
    OverviewCard {
        SectionHeader(title = "History", tag = "Timeline")
        if (isEmpty) {
            Text("No completed workouts", color = OverviewTextColor)
            Text(
                "Design note: highlight empty history with clear path to start logging sessions.",
                color = OverviewMetaColor
            )
            return@OverviewCard
        }

        history.forEach { session ->
            OverviewItem {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Session ${session.id.take(8)}", color = OverviewTextColor, fontWeight = FontWeight.Medium)
                        Text("Exercises: ${session.exerciseLogs.size}", color = OverviewMetaColor)
                    }
                    Button(
                        onClick = { onOpenSessionDetail(session.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = OverviewTextColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF7C3CFF), Color(0xFFD54FFF))))
                    ) {
                        Text("Details")
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
private fun SectionHeader(title: String, tag: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = OverviewTextColor, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(OverviewTagBackground)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(tag, color = OverviewTagText)
        }
    }
}

@Composable
private fun OverviewCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OverviewCardBackground),
        border = BorderStroke(1.dp, OverviewCardBorder),
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

@Composable
private fun OverviewItem(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OverviewItemBackground),
        border = BorderStroke(1.dp, OverviewItemBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
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

private fun formatBookingTime(startMillis: Long, endMillis: Long?): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, MMM d HH:mm")
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(startMillis).atZone(zone).format(formatter)
    val end = endMillis?.let { Instant.ofEpochMilli(it).atZone(zone).format(formatter) }
    return if (end != null) "$start - $end" else start
}
