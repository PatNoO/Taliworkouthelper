package com.example.taliworkouthelper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taliworkouthelper.auth.AuthViewModel
import com.example.taliworkouthelper.auth.FakeAuthRepository
import com.example.taliworkouthelper.auth.LoginScreen
import com.example.taliworkouthelper.auth.RegisterScreen
import com.example.taliworkouthelper.partner.FakePartnerRepository
import com.example.taliworkouthelper.partner.PartnerListScreen
import com.example.taliworkouthelper.partner.PartnerState
import com.example.taliworkouthelper.partner.PartnerViewModel
import com.example.taliworkouthelper.schedule.FakeWorkShiftRepository
import com.example.taliworkouthelper.schedule.WorkScheduleScreen
import com.example.taliworkouthelper.schedule.WorkScheduleViewModel
import com.example.taliworkouthelper.schedule.WorkShift

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepo = FakeAuthRepository()
    val authViewModel = AuthViewModel(authRepo)
    val scheduleVm = WorkScheduleViewModel(FakeWorkShiftRepository())

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLogin = { email, pass ->
                authViewModel.login(email, pass) { _ ->
                    navController.navigate("home")
                }
            }, onNavigateToRegister = { navController.navigate("register") })
        }
        composable("register") {
            RegisterScreen(onRegister = { email, pass ->
                authViewModel.register(email, pass) { _ ->
                    navController.navigate("home")
                }
            }, onNavigateToLogin = { navController.navigate("login") })
        }
        composable("home") {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Welcome to Tali Workout Helper - Home")
                Button(onClick = { navController.navigate("schedule") }) {
                    Text("Open availability")
                }
                Button(onClick = { navController.navigate("partners") }) {
                    Text("Open partners")
                }
            }
        }
        composable("schedule") {
            val state by scheduleVm.state.collectAsState()
            WorkScheduleScreen(
                state = state,
                onAddSampleShift = {
                    val newShift = WorkShift(
                        id = "sample-${System.currentTimeMillis()}",
                        startHour = 9,
                        endHour = 17,
                        dayOfWeek = state.selectedDay
                    )
                    scheduleVm.addShift(newShift) { }
                },
                onRemoveShift = scheduleVm::removeShift,
                onScopeChange = scheduleVm::setAvailabilityScope,
                onDurationChange = scheduleVm::setMinDurationMinutes,
                onDayChange = scheduleVm::setSelectedDay
            )
        }
        composable("partners") {
            val partnerRepo = FakePartnerRepository()
            val partnerVm = PartnerViewModel(partnerRepo)
            val partnersState = partnerVm.state.collectAsState(initial = PartnerState())
            PartnerListScreen(partners = partnersState.value.partners, onConnect = { id ->
                partnerVm.connect(id) { }
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
