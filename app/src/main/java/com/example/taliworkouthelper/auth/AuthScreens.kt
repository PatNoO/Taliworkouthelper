package com.example.taliworkouthelper.auth

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LoginBgTop = Color(0xFF04040A)
private val LoginBgBottom = Color(0xFF0E0D20)
private val LoginPanel = Color(0xC7111025)
private val LoginStroke = Color(0x42B492FF)
private val LoginText = Color(0xFFF4F0FF)
private val LoginMuted = Color(0xFFB8A9DC)
private val LoginViolet = Color(0xFF7C3CFF)
private val LoginFuchsia = Color(0xFFD54FFF)
private val LoginErrorText = Color(0xFFFF7CA5)
private val LoginErrorBg = Color(0x1AFF7CA5)
private val LoginErrorStroke = Color(0x4DFF7CA5)

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onNavigateToRegister: () -> Unit) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(LoginBgTop, LoginBgBottom))
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x807C3CFF), Color.Transparent),
                        radius = 450f
                    )
                )
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LoginStroke),
            colors = CardDefaults.cardColors(containerColor = LoginPanel)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Welcome back", color = LoginText, fontWeight = FontWeight.Bold)
                Text("Sign in to access your workout data", color = LoginMuted)

                AuthInput(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = "Email"
                )
                AuthInput(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = "Password"
                )

                Button(
                    onClick = { onLogin(email.value, password.value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(LoginViolet, LoginFuchsia))),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text("Login")
                }

                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = LoginText
                    ),
                    border = BorderStroke(1.dp, LoginStroke)
                ) {
                    Text("Register")
                }

                // Design notes required by NOO-65 for loading and error states.
                Text("Loading state note: disable inputs and keep button gradient with loading label.", color = LoginMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LoginErrorBg)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        "Error state note: show inline auth error banner without collapsing form layout.",
                        color = LoginErrorText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(onRegister: (String, String) -> Unit, onNavigateToLogin: () -> Unit) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onRegister(email.value, password.value) }, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
        Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) { Text("Back to Login") }
    }
}

@Composable
private fun AuthInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LoginMuted) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LoginStroke,
            unfocusedBorderColor = LoginStroke,
            focusedContainerColor = Color(0x08FFFFFF),
            unfocusedContainerColor = Color(0x08FFFFFF),
            focusedTextColor = LoginText,
            unfocusedTextColor = LoginText,
            focusedLabelColor = LoginText,
            unfocusedLabelColor = LoginMuted
        )
    )
}
