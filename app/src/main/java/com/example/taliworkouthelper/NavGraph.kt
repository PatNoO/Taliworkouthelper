package com.example.taliworkouthelper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taliworkouthelper.auth.AuthViewModel
import com.example.taliworkouthelper.auth.FakeAuthRepository
import com.example.taliworkouthelper.auth.LoginScreen
import com.example.taliworkouthelper.auth.RegisterScreen
import com.example.taliworkouthelper.exercise.ExerciseDetailScreen
import com.example.taliworkouthelper.exercise.ExerciseLibraryScreen
import com.example.taliworkouthelper.exercise.ExerciseLibraryViewModel
import com.example.taliworkouthelper.exercise.FirestoreExerciseRepository
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
    val exerciseViewModel = remember { ExerciseLibraryViewModel(FirestoreExerciseRepository()) }

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
            HomeNavigationScreen(
                onOpenOverview = { navController.navigate("overview") },
                onOpenSchedule = { navController.navigate("schedule") },
                onOpenTrainingRequests = { navController.navigate("trainingRequests") },
                onOpenActiveWorkout = { navController.navigate("activeWorkout") },
                onOpenTemplates = { navController.navigate("templates") },
                onOpenExercises = { navController.navigate("exercises") },
                onOpenPartners = { navController.navigate("partners") }
            )
        }
        composable("exercises") {
            val state by exerciseViewModel.state.collectAsState()
            ExerciseLibraryScreen(
                state = state,
                onSearchQueryChanged = exerciseViewModel::onSearchQueryChanged,
                onSelectMuscleGroup = exerciseViewModel::onSelectMuscleGroup,
                onOpenExerciseDetail = { exerciseId ->
                    exerciseViewModel.onOpenExerciseDetail(exerciseId)
                    navController.navigate("exerciseDetail/$exerciseId")
                },
                onDismissError = exerciseViewModel::dismissError
            )
        }
        composable("exerciseDetail/{exerciseId}") { backStackEntry ->
            val state by exerciseViewModel.state.collectAsState()
            val exerciseId = backStackEntry.arguments?.getString("exerciseId").orEmpty()

            LaunchedEffect(exerciseId) {
                exerciseViewModel.onOpenExerciseDetail(exerciseId)
            }

            ExerciseDetailScreen(
                exercise = state.selectedExercise,
                onBack = {
                    exerciseViewModel.onCloseExerciseDetail()
                    navController.popBackStack()
                }
            )
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
            )
        }
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

private val HomeBackgroundTop = Color(0xFF07070F)
private val HomeBackgroundBottom = Color(0xFF121029)
private val HomeTextColor = Color(0xFFF4F2FF)
private val HomeMutedTextColor = Color(0xFFBCAEDC)
private val HomeCardColor = Color(0xD6161230)
private val HomeCardBorder = Color(0x3DB48CFF)

private data class HomeTile(
    val title: String,
    val subtitle: String,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit
)

@Composable
private fun HomeNavigationScreen(
    onOpenOverview: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenTrainingRequests: () -> Unit,
    onOpenActiveWorkout: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenExercises: () -> Unit,
    onOpenPartners: () -> Unit
) {
    val tiles = listOf(
        HomeTile("Overview", "Upcoming + history snapshot", true, onOpenOverview),
        HomeTile("Work Schedule", "Shifts and availability filters", false, onOpenSchedule),
        HomeTile("Training Requests", "Inbox and pending invites", true, onOpenTrainingRequests),
        HomeTile("Active Workout", "Log sets, reps, notes quickly", false, onOpenActiveWorkout),
        HomeTile("Templates", "Build reusable workout plans", false, onOpenTemplates),
        HomeTile("Exercise Library", "Browse movement references", false, onOpenExercises),
        HomeTile("Partners", "Connect and manage partner links", true, onOpenPartners)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(HomeBackgroundTop, HomeBackgroundBottom))
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x528447FF), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Home", color = HomeTextColor, fontWeight = FontWeight.SemiBold)
            Text("Quick navigation dashboard", color = HomeMutedTextColor)

            tiles.chunked(2).forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTiles.forEach { tile ->
                        HomeTileCard(
                            tile = tile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTiles.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTileCard(tile: HomeTile, modifier: Modifier = Modifier) {
    val gradient = if (tile.isPrimary) {
        Brush.linearGradient(listOf(Color(0x6B7C3CFF), Color(0x3DD54FFF)))
    } else {
        Brush.linearGradient(listOf(HomeCardColor, HomeCardColor))
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HomeCardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(tile.title, color = HomeTextColor, fontWeight = FontWeight.SemiBold)
            Text(tile.subtitle, color = HomeMutedTextColor)
            Button(
                onClick = tile.onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = HomeTextColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7C3CFF), Color(0xFFC04DFF))))
            ) {
                Text("Open")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
