package com.example.taliworkouthelper.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModelTest {
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
    fun `user can log sets reps and optional weight`() = runTest {
        val vm = ActiveWorkoutViewModel(FakeWorkoutSessionRepository())

        vm.onExerciseNameChanged("Bench Press")
        vm.onRepsChanged("8")
        vm.onWeightChanged("70")
        vm.addSetToActiveSession()
        delay(20)

        val active = vm.state.value.activeSession
        assertNotNull(active)
        assertEquals(1, active!!.exerciseLogs.size)
        assertEquals(8, active.exerciseLogs.first().sets.first().reps)
        assertEquals(70.0, active.exerciseLogs.first().sets.first().weightKg)
    }

    @Test
    fun `notes are saved and autosaved`() = runTest {
        val vm = ActiveWorkoutViewModel(FakeWorkoutSessionRepository())

        vm.onExerciseNameChanged("Squat")
        vm.onRepsChanged("5")
        vm.onExerciseNoteChanged("Keep chest up")
        vm.addSetToActiveSession()
        vm.onGeneralNoteChanged("Felt strong")
        delay(20)

        val active = vm.state.value.activeSession
        assertEquals("Keep chest up", active!!.exerciseLogs.first().note)
        assertEquals("Felt strong", active.generalNote)
    }

    @Test
    fun `session can be restored after app restart`() = runTest {
        val repository = FakeWorkoutSessionRepository()
        val session = WorkoutSession(
            id = "s1",
            status = WorkoutSessionStatus.IN_PROGRESS,
            exerciseLogs = listOf(
                ExerciseSessionLog(
                    exerciseName = "Deadlift",
                    sets = listOf(LoggedSet(reps = 5, weightKg = 100.0)),
                    note = "Controlled reps"
                )
            ),
            generalNote = "Existing"
        )
        repository.saveActiveSession(session)

        val vm = ActiveWorkoutViewModel(repository)
        delay(20)

        assertEquals("s1", vm.state.value.activeSession?.id)
        assertEquals("Existing", vm.state.value.activeSession?.generalNote)
    }

    @Test
    fun `completed session appears in workout history`() = runTest {
        val vm = ActiveWorkoutViewModel(FakeWorkoutSessionRepository())

        vm.onExerciseNameChanged("Row")
        vm.onRepsChanged("10")
        vm.addSetToActiveSession()
        delay(20)

        vm.completeActiveSession()
        delay(20)

        assertNull(vm.state.value.activeSession)
        assertTrue(vm.state.value.history.isNotEmpty())
        assertEquals(WorkoutSessionStatus.COMPLETED, vm.state.value.history.first().status)
    }
}
