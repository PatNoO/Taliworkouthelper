package com.example.taliworkouthelper.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkScheduleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fullDayShift_showsEmptyState() {
        composeTestRule.setContent {
            var shifts by remember {
                mutableStateOf(listOf(WorkShift("s-test", 6, 22, DayOfWeek.MONDAY)))
            }
            var scope by remember { mutableStateOf(AvailabilityScope.DAY) }
            var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
            var duration by remember { mutableIntStateOf(45) }

            fun buildState(): WorkScheduleState {
                return WorkScheduleState(
                    shifts = shifts,
                    availabilityScope = scope,
                    selectedDay = selectedDay,
                    minDurationMinutes = duration,
                    availableSlots = AvailabilityCalculator.calculate(shifts, scope, selectedDay, duration)
                )
            }

            var state by remember { mutableStateOf(buildState()) }

            WorkScheduleScreen(
                state = state,
                onAddSampleShift = {
                    shifts = shifts + WorkShift("new", 9, 17, selectedDay)
                    state = buildState()
                },
                onRemoveShift = { id ->
                    shifts = shifts.filterNot { it.id == id }
                    state = buildState()
                },
                onScopeChange = {
                    scope = it
                    state = buildState()
                },
                onDurationChange = {
                    duration = it
                    state = buildState()
                },
                onDayChange = {
                    selectedDay = it
                    state = buildState()
                }
            )
        }

        assertEquals(
            1,
            composeTestRule
                .onAllNodesWithText("No available slots for selected filters.")
                .fetchSemanticsNodes().size
        )
    }

    @Test
    fun durationFilter_hidesShortSlots() {
        composeTestRule.setContent {
            var shifts by remember {
                mutableStateOf(listOf(WorkShift("s-test", 7, 21, DayOfWeek.MONDAY)))
            }
            var scope by remember { mutableStateOf(AvailabilityScope.DAY) }
            var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
            var duration by remember { mutableIntStateOf(45) }

            fun buildState(): WorkScheduleState {
                return WorkScheduleState(
                    shifts = shifts,
                    availabilityScope = scope,
                    selectedDay = selectedDay,
                    minDurationMinutes = duration,
                    availableSlots = AvailabilityCalculator.calculate(shifts, scope, selectedDay, duration)
                )
            }

            var state by remember { mutableStateOf(buildState()) }

            WorkScheduleScreen(
                state = state,
                onAddSampleShift = {
                    shifts = shifts + WorkShift("new", 9, 17, selectedDay)
                    state = buildState()
                },
                onRemoveShift = { id ->
                    shifts = shifts.filterNot { it.id == id }
                    state = buildState()
                },
                onScopeChange = {
                    scope = it
                    state = buildState()
                },
                onDurationChange = {
                    duration = it
                    state = buildState()
                },
                onDayChange = {
                    selectedDay = it
                    state = buildState()
                }
            )
        }

        composeTestRule.onNodeWithText("90 min").performClick()

        assertEquals(
            0,
            composeTestRule
                .onAllNodesWithText("Mon: 6:00-7:00 (60 min)")
                .fetchSemanticsNodes().size
        )
        assertEquals(
            1,
            composeTestRule
                .onAllNodesWithText("No available slots for selected filters.")
                .fetchSemanticsNodes().size
        )
    }
}
