package com.example.taliworkouthelper.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onNavigateToRegister: () -> Unit) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { onLogin(email.value, password.value) }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Button(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) { Text("Register") }
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
