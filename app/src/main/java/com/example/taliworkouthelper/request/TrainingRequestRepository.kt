package com.example.taliworkouthelper.request

import kotlinx.coroutines.flow.Flow

interface TrainingRequestRepository {
    fun observeIncomingRequests(): Flow<List<TrainingRequest>>
    fun observeOutgoingRequests(): Flow<List<TrainingRequest>>
    fun observeBookings(): Flow<List<Booking>>
    suspend fun sendRequest(toUid: String, startEpochMillis: Long, endEpochMillis: Long): Result<TrainingRequest>
    suspend fun acceptRequest(requestId: String): Result<TrainingRequest>
    suspend fun declineRequest(requestId: String): Result<TrainingRequest>
}
