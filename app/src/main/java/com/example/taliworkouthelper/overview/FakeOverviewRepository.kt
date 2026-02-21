package com.example.taliworkouthelper.overview

import com.example.taliworkouthelper.session.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeOverviewRepository(
    initialBookings: List<UpcomingBooking> = emptyList(),
    initialHistory: List<WorkoutSession> = emptyList()
) : OverviewRepository {

    private val bookingsFlow = MutableStateFlow(initialBookings)
    private val historyFlow = MutableStateFlow(initialHistory)

    override fun observeUpcomingBookings(limit: Int): Flow<List<UpcomingBooking>> = bookingsFlow

    override fun observeWorkoutHistory(limit: Int): Flow<List<WorkoutSession>> = historyFlow

    fun setBookings(bookings: List<UpcomingBooking>) {
        bookingsFlow.value = bookings
    }

    fun setHistory(history: List<WorkoutSession>) {
        historyFlow.value = history
    }
}
