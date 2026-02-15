package com.example.taliworkouthelper

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import com.example.taliworkouthelper.partner.Partner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taliworkouthelper.auth.AuthViewModel
import com.example.taliworkouthelper.auth.FakeAuthRepository
import com.example.taliworkouthelper.auth.LoginScreen
import com.example.taliworkouthelper.auth.RegisterScreen
import com.example.taliworkouthelper.partner.FakePartnerRepository
import com.example.taliworkouthelper.partner.PartnerListScreen
import com.example.taliworkouthelper.partner.PartnerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val repo = FakeAuthRepository()
    val viewModel = AuthViewModel(repo)

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(onLogin = { email, pass ->
                viewModel.login(email, pass) { _ ->
                    navController.navigate("home")
                }
            }, onNavigateToRegister = { navController.navigate("register") })
        }
        composable("register") {
            RegisterScreen(onRegister = { email, pass ->
                viewModel.register(email, pass) { _ ->
                    navController.navigate("home")
                }
            }, onNavigateToLogin = { navController.navigate("login") })
        }
        composable("home") {
            Text("Welcome to Tali Workout Helper - Home")
        }
        composable("partners") {
            val partnerRepo = FakePartnerRepository()
            val partnerVm = PartnerViewModel(partnerRepo)
            val partnersState = partnerVm.state.collectAsState(initial = com.example.taliworkouthelper.partner.PartnerState())
            PartnerListScreen(partners = partnersState.value.partners, onConnect = { id ->
                partnerVm.connect(id) { /* no-op for preview */ }
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavHostPreview() {
    AppNavHost()
}
