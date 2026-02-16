package com.example.taliworkouthelper.schedule

import java.time.DayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkScheduleViewModelTest {
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
    fun `add and remove shift`() = runBlocking {
        val repo = FakeWorkShiftRepository()
        val vm = WorkScheduleViewModel(repo)

        assertTrue(vm.state.value.shifts.isEmpty())

        var res: Result<WorkShift>? = null
        vm.addShift(WorkShift("s1", 9, 17, DayOfWeek.MONDAY)) { res = it }
        delay(10)
        assertNotNull(res)
        assertTrue(res!!.isSuccess)
        assertEquals(1, vm.state.value.shifts.size)

        vm.removeShift("s1")
        delay(10)
        assertTrue(vm.state.value.shifts.isEmpty())
    }

    @Test
    fun `duration filter updates available slots`() = runBlocking {
        val repo = FakeWorkShiftRepository()
        val vm = WorkScheduleViewModel(repo)

        vm.addShift(WorkShift("s1", 6, 7, DayOfWeek.MONDAY)) { }
        vm.addShift(WorkShift("s2", 8, 22, DayOfWeek.MONDAY)) { }
        delay(10)

        vm.setMinDurationMinutes(90)
        delay(10)

        assertTrue(vm.state.value.availableSlots.isEmpty())
    }

    @Test
    fun `week scope includes non working days`() = runBlocking {
        val repo = FakeWorkShiftRepository()
        val vm = WorkScheduleViewModel(repo)

        vm.addShift(WorkShift("s1", 9, 17, DayOfWeek.MONDAY)) { }
        vm.setAvailabilityScope(AvailabilityScope.WEEK)
        delay(10)

        val hasTuesdayFullAvailability = vm.state.value.availableSlots.any {
            it.dayOfWeek == DayOfWeek.TUESDAY && it.startHour == 6 && it.endHour == 22
        }
        assertTrue(hasTuesdayFullAvailability)
    }
}
