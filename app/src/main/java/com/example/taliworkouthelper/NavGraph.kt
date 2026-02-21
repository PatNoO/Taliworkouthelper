package com.example.taliworkouthelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taliworkouthelper.auth.AuthViewModel
import com.example.taliworkouthelper.auth.FakeAuthRepository
import com.example.taliworkouthelper.auth.LoginScreen
import com.example.taliworkouthelper.auth.RegisterScreen
import com.example.taliworkouthelper.overview.FirestoreOverviewRepository
import com.example.taliworkouthelper.overview.OverviewScreen
import com.example.taliworkouthelper.overview.OverviewViewModel
import com.example.taliworkouthelper.overview.WorkoutSessionDetailScreen
import com.example.taliworkouthelper.partner.FakePartnerRepository
import com.example.taliworkouthelper.partner.PartnerListScreen
import com.example.taliworkouthelper.partner.PartnerState
import com.example.taliworkouthelper.partner.PartnerViewModel
import com.example.taliworkouthelper.request.FirestoreTrainingRequestRepository
import com.example.taliworkouthelper.request.TrainingRequestScreen
import com.example.taliworkouthelper.request.TrainingRequestViewModel
import com.example.taliworkouthelper.schedule.FirestoreWorkScheduleRepository
import com.example.taliworkouthelper.schedule.WorkScheduleScreen
import com.example.taliworkouthelper.schedule.WorkScheduleViewModel
import com.example.taliworkouthelper.session.ActiveWorkoutScreen
import com.example.taliworkouthelper.session.ActiveWorkoutViewModel
import com.example.taliworkouthelper.session.FirestoreWorkoutSessionRepository
import com.example.taliworkouthelper.template.FirestoreWorkoutTemplateRepository
import com.example.taliworkouthelper.template.WorkoutTemplateScreen
import com.example.taliworkouthelper.template.WorkoutTemplateViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepo = FakeAuthRepository()
    val authViewModel = remember { AuthViewModel(authRepo) }
    val scheduleViewModel = remember { WorkScheduleViewModel(FirestoreWorkScheduleRepository()) }
    val trainingRequestViewModel = remember { TrainingRequestViewModel(FirestoreTrainingRequestRepository()) }
    val activeWorkoutViewModel = remember { ActiveWorkoutViewModel(FirestoreWorkoutSessionRepository()) }
    val templateViewModel = remember { WorkoutTemplateViewModel(FirestoreWorkoutTemplateRepository()) }
    val overviewViewModel = remember { OverviewViewModel(FirestoreOverviewRepository()) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLogin = { email, pass ->
                    authViewModel.login(email, pass) {
                        navController.navigate("home")
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegister = { email, pass ->
                    authViewModel.register(email, pass) {
                        navController.navigate("home")
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("home") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Welcome to Tali Workout Helper - Home")
                Button(onClick = { navController.navigate("overview") }) {
                    Text("Open overview")
                }
                Button(onClick = { navController.navigate("schedule") }) {
                    Text("Open work schedule")
                }
                Button(onClick = { navController.navigate("trainingRequests") }) {
                    Text("Open training requests")
                Button(onClick = { navController.navigate("activeWorkout") }) {
                    Text("Open active workout")
                }
                Button(onClick = { navController.navigate("templates") }) {
                    Text("Open workout templates")
                }
                Button(onClick = { navController.navigate("partners") }) {
                    Text("Open partners")
                }
            }
        }
        composable("overview") {
            val state by overviewViewModel.state.collectAsState()
            OverviewScreen(
                state = state,
                onOpenSessionDetail = { sessionId ->
                    overviewViewModel.selectSession(sessionId)
                    navController.navigate("sessionDetail/$sessionId")
                },
                onDismissError = overviewViewModel::dismissError
            )
        }
        composable("sessionDetail/{sessionId}") {
            val state by overviewViewModel.state.collectAsState()
            WorkoutSessionDetailScreen(
                session = state.selectedSession,
                onBack = {
                    overviewViewModel.clearSelectedSession()
                    navController.popBackStack()
                }
            )
        }
        composable("schedule") {
            val state by scheduleViewModel.state.collectAsState()
            WorkScheduleScreen(
                state = state,
                onStartHourChanged = scheduleViewModel::onStartHourChanged,
                onEndHourChanged = scheduleViewModel::onEndHourChanged,
                onFormDayChange = scheduleViewModel::onFormDayChanged,
                onSubmitShift = scheduleViewModel::onSubmitShift,
                onCancelEdit = scheduleViewModel::onCancelEdit,
                onEditShift = scheduleViewModel::onEditShift,
                onDeleteShift = scheduleViewModel::removeShift,
                onScopeChange = scheduleViewModel::setAvailabilityScope,
                onDurationChange = scheduleViewModel::setMinDurationMinutes,
                onDayChange = scheduleViewModel::setSelectedDay,
                onDismissError = scheduleViewModel::dismissError
            )
        }
        composable("trainingRequests") {
            val state by trainingRequestViewModel.state.collectAsState()
            TrainingRequestScreen(
                state = state,
                formatEpoch = trainingRequestViewModel::formatEpoch,
                onPartnerUidChanged = trainingRequestViewModel::onPartnerUidChanged,
                onStartChanged = trainingRequestViewModel::onStartChanged,
                onEndChanged = trainingRequestViewModel::onEndChanged,
                onSendRequest = trainingRequestViewModel::sendRequest,
                onAcceptRequest = trainingRequestViewModel::acceptRequest,
                onDeclineRequest = trainingRequestViewModel::declineRequest,
                onDismissError = trainingRequestViewModel::dismissError
        composable("activeWorkout") {
            val state by activeWorkoutViewModel.state.collectAsState()
            ActiveWorkoutScreen(
                state = state,
                onExerciseNameChanged = activeWorkoutViewModel::onExerciseNameChanged,
                onRepsChanged = activeWorkoutViewModel::onRepsChanged,
                onWeightChanged = activeWorkoutViewModel::onWeightChanged,
                onExerciseNoteChanged = activeWorkoutViewModel::onExerciseNoteChanged,
                onGeneralNoteChanged = activeWorkoutViewModel::onGeneralNoteChanged,
                onAddSet = activeWorkoutViewModel::addSetToActiveSession,
                onCompleteSession = activeWorkoutViewModel::completeActiveSession,
                onDismissError = activeWorkoutViewModel::dismissError
            )
        }
        composable("templates") {
            val state by templateViewModel.state.collectAsState()
            WorkoutTemplateScreen(
                state = state,
                onTitleChanged = templateViewModel::onTitleChanged,
                onExerciseNameChanged = templateViewModel::onExerciseNameChanged,
                onExerciseSetsChanged = templateViewModel::onExerciseSetsChanged,
                onExerciseRepsChanged = templateViewModel::onExerciseRepsChanged,
                onAddExercise = templateViewModel::addExerciseToForm,
                onRemoveExercise = templateViewModel::removeExerciseFromForm,
                onSaveTemplate = templateViewModel::onSaveTemplate,
                onEditTemplate = templateViewModel::onEditTemplate,
                onCancelEdit = templateViewModel::onCancelEdit,
                onDismissError = templateViewModel::dismissError
            )
        }
        composable("partners") {
            val partnerRepo = FakePartnerRepository()
            val partnerVm = PartnerViewModel(partnerRepo)
            val partnersState = partnerVm.state.collectAsState(initial = PartnerState())
            PartnerListScreen(
                partners = partnersState.value.partners,
                onConnect = { partnerId ->
                    partnerVm.connect(partnerId) { }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
