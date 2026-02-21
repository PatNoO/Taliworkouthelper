package com.example.taliworkouthelper.request

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
fun TrainingRequestScreen(
    state: TrainingRequestUiState,
    formatEpoch: (Long) -> String,
    onPartnerUidChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onSendRequest: () -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Training Requests")
        Text("Inbox notifications: ${state.notificationCount}")

        if (state.errorMessage != null) {
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Error: ${state.errorMessage}")
                    Button(onClick = onDismissError) { Text("Dismiss") }
                }
            }
        }

        RequestProposalForm(
            state = state,
            onPartnerUidChanged = onPartnerUidChanged,
            onStartChanged = onStartChanged,
            onEndChanged = onEndChanged,
            onSendRequest = onSendRequest
        )

        IncomingRequestsSection(
            incoming = state.incoming,
            formatEpoch = formatEpoch,
            onAcceptRequest = onAcceptRequest,
            onDeclineRequest = onDeclineRequest
        )

        OutgoingRequestsSection(
            outgoing = state.outgoing,
            formatEpoch = formatEpoch
        )

        BookingsSection(
            bookings = state.bookings,
            formatEpoch = formatEpoch
        )
    }
}

@Composable
private fun RequestProposalForm(
    state: TrainingRequestUiState,
    onPartnerUidChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onSendRequest: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Send request")
            OutlinedTextField(
                value = state.form.partnerUidInput,
                onValueChange = onPartnerUidChanged,
                label = { Text("Partner uid") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.form.startInput,
                onValueChange = onStartChanged,
                label = { Text("Start (yyyy-MM-ddTHH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.form.endInput,
                onValueChange = onEndChanged,
                label = { Text("End (yyyy-MM-ddTHH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(onClick = onSendRequest, enabled = !state.isSubmitting) {
                Text(if (state.isSubmitting) "Sending..." else "Send training request")
            }
        }
    }
}

@Composable
private fun IncomingRequestsSection(
    incoming: List<TrainingRequest>,
    formatEpoch: (Long) -> String,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit
) {
    Text("Incoming requests")
    if (incoming.isEmpty()) {
        Text("No incoming requests")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(incoming, key = { it.id }) { request ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("From: ${request.fromUid}")
                    Text("Start: ${formatEpoch(request.startEpochMillis)}")
                    Text("End: ${formatEpoch(request.endEpochMillis)}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onAcceptRequest(request.id) }) { Text("Accept") }
                        Button(onClick = { onDeclineRequest(request.id) }) { Text("Decline") }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutgoingRequestsSection(
    outgoing: List<TrainingRequest>,
    formatEpoch: (Long) -> String
) {
    Text("Outgoing requests")
    if (outgoing.isEmpty()) {
        Text("No outgoing requests")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(outgoing, key = { it.id }) { request ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("To: ${request.toUid}")
                    Text("Start: ${formatEpoch(request.startEpochMillis)}")
                    Text("End: ${formatEpoch(request.endEpochMillis)}")
                    Text("Status: ${request.status}")
                }
            }
        }
    }
}

@Composable
private fun BookingsSection(
    bookings: List<Booking>,
    formatEpoch: (Long) -> String
) {
    Text("Synced bookings")
    if (bookings.isEmpty()) {
        Text("No bookings yet")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(bookings, key = { it.id }) { booking ->
            Card {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Partner: ${booking.partnerUid}")
                    Text("Start: ${formatEpoch(booking.startEpochMillis)}")
                    Text("End: ${formatEpoch(booking.endEpochMillis)}")
                    Text("From request: ${booking.requestId.take(8)}")
                }
            }
        }
    }
}
