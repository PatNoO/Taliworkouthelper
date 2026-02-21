package com.example.taliworkouthelper.overview

import com.example.taliworkouthelper.session.ExerciseSessionLog
import com.example.taliworkouthelper.session.LoggedSet
import com.example.taliworkouthelper.session.WorkoutSession
import com.example.taliworkouthelper.session.WorkoutSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads bookings and workout history`() = runTest {
        val repository = FakeOverviewRepository(
            initialBookings = listOf(UpcomingBooking("b1", "Alex", 1000L, 2000L, "Upper body")),
            initialHistory = listOf(sampleSession("s1"))
        )
        val vm = OverviewViewModel(repository)
        delay(20)

        assertFalse(vm.state.value.isLoading)
        assertEquals(1, vm.state.value.upcomingBookings.size)
        assertEquals(1, vm.state.value.workoutHistory.size)
    }

    @Test
    fun `loading completes with empty states when no data exists`() = runTest {
        val vm = OverviewViewModel(FakeOverviewRepository())
        delay(20)

        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.isEmptyBookings)
        assertTrue(vm.state.value.isEmptyWorkoutHistory)
    }

    @Test
    fun `select session exposes detail`() = runTest {
        val repository = FakeOverviewRepository(initialHistory = listOf(sampleSession("s1")))
        val vm = OverviewViewModel(repository)
        delay(20)

        vm.selectSession("s1")

        assertEquals("s1", vm.state.value.selectedSession?.id)
    }

    @Test
    fun `selected session clears when not present anymore`() = runTest {
        val repository = FakeOverviewRepository(initialHistory = listOf(sampleSession("s1")))
        val vm = OverviewViewModel(repository)
        delay(20)

        vm.selectSession("s1")
        repository.setHistory(emptyList())
        delay(20)

        assertNull(vm.state.value.selectedSession)
        assertTrue(vm.state.value.isEmptyWorkoutHistory)
    }

    private fun sampleSession(id: String): WorkoutSession {
        return WorkoutSession(
            id = id,
            status = WorkoutSessionStatus.COMPLETED,
            exerciseLogs = listOf(
                ExerciseSessionLog(
                    exerciseName = "Bench press",
                    sets = listOf(LoggedSet(reps = 10, weightKg = 60.0)),
                    note = "Felt strong"
                )
            ),
            generalNote = "Good session"
        )
    }
}
