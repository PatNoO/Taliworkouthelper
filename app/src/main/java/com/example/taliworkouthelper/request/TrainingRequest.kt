package com.example.taliworkouthelper.request

enum class TrainingRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

data class TrainingRequest(
    val id: String,
    val fromUid: String,
    val toUid: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val status: TrainingRequestStatus
)

data class Booking(
    val id: String,
    val ownerUid: String,
    val partnerUid: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val requestId: String
)
