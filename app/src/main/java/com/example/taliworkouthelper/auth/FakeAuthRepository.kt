package com.example.taliworkouthelper.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    private val _user = MutableStateFlow<User?>(null)
    override fun currentUser(): Flow<User?> = _user

    override suspend fun register(email: String, password: String): Result<User> {
        val user = User(uid = email, email = email)
        _user.value = user
        return Result.success(user)
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val user = User(uid = email, email = email)
        _user.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _user.value = null
    }
}
