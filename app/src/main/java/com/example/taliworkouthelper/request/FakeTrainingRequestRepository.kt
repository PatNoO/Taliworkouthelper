package com.example.taliworkouthelper.request

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrainingRequestRepository(
    private val currentUid: String = "me"
) : TrainingRequestRepository {
    private val requestsState = MutableStateFlow<List<TrainingRequest>>(emptyList())
    private val bookingsState = MutableStateFlow<List<Booking>>(emptyList())

    override fun observeIncomingRequests(): Flow<List<TrainingRequest>> {
        return requestsState.map { list ->
            list.filter { it.toUid == currentUid && it.status == TrainingRequestStatus.PENDING }
        }
    }

    override fun observeOutgoingRequests(): Flow<List<TrainingRequest>> {
        return requestsState.map { list -> list.filter { it.fromUid == currentUid } }
    }

    override fun observeBookings(): Flow<List<Booking>> {
        return bookingsState.map { list -> list.filter { it.ownerUid == currentUid } }
    }

    override suspend fun sendRequest(
        toUid: String,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Result<TrainingRequest> {
        if (endEpochMillis <= startEpochMillis) {
            return Result.failure(IllegalArgumentException("End time must be after start time"))
        }
        if (hasConflict(currentUid, startEpochMillis, endEpochMillis) || hasConflict(toUid, startEpochMillis, endEpochMillis)) {
            return Result.failure(IllegalStateException("Booking conflict detected"))
        }

        val request = TrainingRequest(
            id = UUID.randomUUID().toString(),
            fromUid = currentUid,
            toUid = toUid,
            startEpochMillis = startEpochMillis,
            endEpochMillis = endEpochMillis,
            status = TrainingRequestStatus.PENDING
        )
        requestsState.value = requestsState.value + request
        return Result.success(request)
    }

    override suspend fun acceptRequest(requestId: String): Result<TrainingRequest> {
        val request = requestsState.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(IllegalArgumentException("Request not found"))

        if (hasConflict(request.fromUid, request.startEpochMillis, request.endEpochMillis) ||
            hasConflict(request.toUid, request.startEpochMillis, request.endEpochMillis)
        ) {
            return Result.failure(IllegalStateException("Booking conflict detected"))
        }

        val accepted = request.copy(status = TrainingRequestStatus.ACCEPTED)
        requestsState.value = requestsState.value.map { if (it.id == requestId) accepted else it }

        val firstBooking = Booking(
            id = UUID.randomUUID().toString(),
            ownerUid = request.fromUid,
            partnerUid = request.toUid,
            startEpochMillis = request.startEpochMillis,
            endEpochMillis = request.endEpochMillis,
            requestId = request.id
        )
        val secondBooking = firstBooking.copy(
            id = UUID.randomUUID().toString(),
            ownerUid = request.toUid,
            partnerUid = request.fromUid
        )
        bookingsState.value = bookingsState.value + listOf(firstBooking, secondBooking)

        return Result.success(accepted)
    }

    override suspend fun declineRequest(requestId: String): Result<TrainingRequest> {
        val request = requestsState.value.firstOrNull { it.id == requestId }
            ?: return Result.failure(IllegalArgumentException("Request not found"))
        val declined = request.copy(status = TrainingRequestStatus.DECLINED)
        requestsState.value = requestsState.value.map { if (it.id == requestId) declined else it }
        return Result.success(declined)
    }

    private fun hasConflict(userId: String, startMillis: Long, endMillis: Long): Boolean {
        return bookingsState.value.any { booking ->
            booking.ownerUid == userId && startMillis < booking.endEpochMillis && booking.startEpochMillis < endMillis
        }
    }
}
