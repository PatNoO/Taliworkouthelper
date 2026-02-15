package com.example.taliworkouthelper.auth

import kotlinx.coroutines.flow.Flow

data class User(val uid: String, val email: String)

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    fun currentUser(): Flow<User?>
}
