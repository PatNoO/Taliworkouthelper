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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.taliworkouthelper.schedule.FirestoreWorkScheduleRepository
import com.example.taliworkouthelper.schedule.WorkScheduleScreen
import com.example.taliworkouthelper.schedule.WorkScheduleViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepo = FakeAuthRepository()
    val authViewModel = AuthViewModel(authRepo)
    val scheduleViewModel = WorkScheduleViewModel(FirestoreWorkScheduleRepository())

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
                Button(onClick = { navController.navigate("schedule") }) {
                    Text("Open work schedule")
                }
                Button(onClick = { navController.navigate("partners") }) {
                    Text("Open partners")
                }
            }
        }
        composable("schedule") {
            val state by scheduleViewModel.state.collectAsState()
            WorkScheduleScreen(
                state = state,
                onStartHourChanged = scheduleViewModel::onStartHourChanged,
                onEndHourChanged = scheduleViewModel::onEndHourChanged,
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
