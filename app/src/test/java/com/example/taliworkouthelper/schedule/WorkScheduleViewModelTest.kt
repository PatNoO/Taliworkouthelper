package com.example.taliworkouthelper.schedule

import java.time.DayOfWeek
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
    fun `submit valid shift adds item and clears form`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("9")
        vm.onEndHourChanged("17")
        vm.onSubmitShift()
        delay(20)

        assertEquals(1, vm.state.value.shifts.size)
        assertEquals("", vm.state.value.form.startHourInput)
        assertEquals("", vm.state.value.form.endHourInput)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `invalid range sets validation error`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("18")
        vm.onEndHourChanged("10")
        vm.onSubmitShift()

        assertEquals("End hour must be after start hour", vm.state.value.errorMessage)
        assertTrue(vm.state.value.shifts.isEmpty())
    }

    @Test
    fun `edit updates existing shift`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("8")
        vm.onEndHourChanged("10")
        vm.onSubmitShift()
        delay(20)

        val created = vm.state.value.shifts.first()
        vm.onEditShift(created)
        vm.onEndHourChanged("11")
        vm.onSubmitShift()
        delay(20)

        assertEquals(1, vm.state.value.shifts.size)
        assertEquals(11, vm.state.value.shifts.first().endHour)
        assertFalse(vm.state.value.form.isEditing)
    }

    @Test
    fun `form day selection controls persisted shift day`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onFormDayChanged(DayOfWeek.WEDNESDAY)
        vm.onStartHourChanged("9")
        vm.onEndHourChanged("11")
        vm.onSubmitShift()
        delay(20)

        vm.setSelectedDay(DayOfWeek.MONDAY)
        val created = vm.state.value.shifts.first()
        assertEquals(DayOfWeek.WEDNESDAY, created.dayOfWeek)
    }

    @Test
    fun `remove deletes existing shift`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("6")
        vm.onEndHourChanged("8")
        vm.onSubmitShift()
        delay(20)

        val createdId = vm.state.value.shifts.first().id
        vm.removeShift(createdId)
        delay(20)

        assertTrue(vm.state.value.shifts.isEmpty())
    }

    @Test
    fun `duration filter updates available slots`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("6")
        vm.onEndHourChanged("7")
        vm.onSubmitShift()
        delay(20)

        vm.onStartHourChanged("8")
        vm.onEndHourChanged("22")
        vm.onSubmitShift()
        delay(20)

        vm.setMinDurationMinutes(90)
        delay(20)

        assertTrue(vm.state.value.availableSlots.isEmpty())
    }

    @Test
    fun `week scope includes non working days`() = runTest {
        val vm = WorkScheduleViewModel(FakeWorkShiftRepository())

        vm.onStartHourChanged("9")
        vm.onEndHourChanged("17")
        vm.onSubmitShift()
        vm.setAvailabilityScope(AvailabilityScope.WEEK)
        delay(20)

        val hasTuesdayFullAvailability = vm.state.value.availableSlots.any {
            it.dayOfWeek == DayOfWeek.TUESDAY && it.startHour == 6 && it.endHour == 22
        }
        assertTrue(hasTuesdayFullAvailability)
    }
}
