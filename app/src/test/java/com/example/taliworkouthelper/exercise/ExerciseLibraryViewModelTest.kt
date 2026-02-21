package com.example.taliworkouthelper.exercise

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
class ExerciseLibraryViewModelTest {
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
    fun `loads exercises and exposes filters`() = runTest {
        val repository = FakeExerciseRepository(sampleExercises())

        val vm = ExerciseLibraryViewModel(repository)
        delay(20)

        assertFalse(vm.state.value.isLoading)
        assertEquals(3, vm.state.value.filteredExercises.size)
        assertTrue(vm.state.value.muscleGroupFilters.contains("All"))
        assertTrue(vm.state.value.muscleGroupFilters.contains("Legs"))
        assertTrue(vm.state.value.muscleGroupFilters.contains("Chest"))
    }

    @Test
    fun `search filters list by name and description`() = runTest {
        val vm = ExerciseLibraryViewModel(FakeExerciseRepository(sampleExercises()))
        delay(20)

        vm.onSearchQueryChanged("barbell")

        assertEquals(2, vm.state.value.filteredExercises.size)
        assertTrue(vm.state.value.filteredExercises.any { it.id == "squat" })
        assertTrue(vm.state.value.filteredExercises.any { it.id == "bench" })
    }

    @Test
    fun `muscle group filter combines with search`() = runTest {
        val vm = ExerciseLibraryViewModel(FakeExerciseRepository(sampleExercises()))
        delay(20)

        vm.onSelectMuscleGroup("Legs")
        vm.onSearchQueryChanged("bridge")

        assertEquals(1, vm.state.value.filteredExercises.size)
        assertEquals("glute_bridge", vm.state.value.filteredExercises.first().id)
    }

    @Test
    fun `detail selection clears when exercise disappears`() = runTest {
        val repository = FakeExerciseRepository(sampleExercises())
        val vm = ExerciseLibraryViewModel(repository)
        delay(20)

        vm.onOpenExerciseDetail("bench")
        assertEquals("bench", vm.state.value.selectedExercise?.id)

        repository.setExercises(sampleExercises().filterNot { it.id == "bench" })
        delay(20)

        assertNull(vm.state.value.selectedExercise)
    }

    private fun sampleExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = "squat",
                name = "Back Squat",
                description = "Barbell squat for lower body strength",
                muscleGroup = "Legs",
                equipment = "Barbell",
                imageUrl = "https://example.com/squat.png"
            ),
            Exercise(
                id = "bench",
                name = "Bench Press",
                description = "Horizontal barbell pressing movement",
                muscleGroup = "Chest",
                equipment = "Barbell",
                imageUrl = "https://example.com/bench.png"
            ),
            Exercise(
                id = "glute_bridge",
                name = "Glute Bridge",
                description = "Bodyweight glute activation exercise",
                muscleGroup = "Legs",
                equipment = "Bodyweight",
                imageUrl = ""
            )
        )
    }
}
