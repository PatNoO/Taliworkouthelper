package com.example.taliworkouthelper.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthState(val user: User?, val loading: Boolean = false, val error: String? = null)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    val state: StateFlow<AuthState> = repo.currentUser().map { user ->
        AuthState(user = user)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthState(user = null))

    fun register(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val res = repo.register(email, password)
            onResult(res)
        }
    }

    fun login(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val res = repo.login(email, password)
            onResult(res)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }
}
