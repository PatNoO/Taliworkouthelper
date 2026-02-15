package com.example.taliworkouthelper.schedule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
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
        vm.addShift(WorkShift("s1", 9, 17)) { res = it }
        kotlinx.coroutines.delay(10)
        assertNotNull(res)
        assertTrue(res!!.isSuccess)
        assertEquals(1, vm.state.value.shifts.size)

        vm.removeShift("s1")
        kotlinx.coroutines.delay(10)
        assertTrue(vm.state.value.shifts.isEmpty())
    }
}
