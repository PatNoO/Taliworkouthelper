package com.example.taliworkouthelper.overview

import com.example.taliworkouthelper.session.WorkoutSession
import kotlinx.coroutines.flow.Flow

data class UpcomingBooking(
    val id: String,
    val partnerName: String,
    val startAtMillis: Long,
    val endAtMillis: Long?,
    val title: String = ""
)

interface OverviewRepository {
    fun observeUpcomingBookings(limit: Int = 20): Flow<List<UpcomingBooking>>
    fun observeWorkoutHistory(limit: Int = 20): Flow<List<WorkoutSession>>
}
