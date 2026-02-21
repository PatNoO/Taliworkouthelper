package com.example.taliworkouthelper.template

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
class WorkoutTemplateViewModelTest {
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
    fun `cannot save template without exercises`() = runTest {
        val vm = WorkoutTemplateViewModel(FakeWorkoutTemplateRepository())

        vm.onTitleChanged("Push Day")
        vm.onSaveTemplate()

        assertEquals("Template must include at least one exercise", vm.state.value.errorMessage)
        assertTrue(vm.state.value.templates.isEmpty())
    }

    @Test
    fun `save template with exercises succeeds`() = runTest {
        val vm = WorkoutTemplateViewModel(FakeWorkoutTemplateRepository())

        vm.onTitleChanged("Leg Day")
        vm.onExerciseNameChanged("Squat")
        vm.onExerciseSetsChanged("4")
        vm.onExerciseRepsChanged("8")
        vm.addExerciseToForm()
        vm.onSaveTemplate()
        delay(20)

        assertEquals(1, vm.state.value.templates.size)
        assertEquals("Leg Day", vm.state.value.templates.first().title)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun `saved template can be reopened and edited`() = runTest {
        val vm = WorkoutTemplateViewModel(FakeWorkoutTemplateRepository())

        vm.onTitleChanged("Upper")
        vm.onExerciseNameChanged("Bench")
        vm.onExerciseSetsChanged("3")
        vm.onExerciseRepsChanged("10")
        vm.addExerciseToForm()
        vm.onSaveTemplate()
        delay(20)

        val created = vm.state.value.templates.first()
        vm.onEditTemplate(created)
        vm.onTitleChanged("Upper Strength")
        vm.onSaveTemplate()
        delay(20)

        assertEquals(1, vm.state.value.templates.size)
        assertEquals("Upper Strength", vm.state.value.templates.first().title)
        assertNotNull(vm.state.value.templates.first().exercises.firstOrNull())
    }
}
