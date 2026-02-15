package com.example.taliworkouthelper.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fake repository register login logout and viewmodel state`() = runBlocking {
        val repo = FakeAuthRepository()
        val vm = AuthViewModel(repo)

        // Initially no user
        assertNull(repo.currentUser().first())
        assertNull(vm.state.value.user)

        // Register
        var registerResult: Result<User>? = null
        vm.register("test@example.com", "password") { res -> registerResult = res }
        // give coroutine a moment
        kotlinx.coroutines.delay(10)
        assertNotNull(registerResult)
        assertTrue(registerResult!!.isSuccess)
        val regUser = registerResult!!.getOrNull()!!
        assertEquals("test@example.com", regUser.email)
        // ViewModel state should reflect
        assertEquals("test@example.com", vm.state.value.user?.email)

        // Logout
        vm.logout()
        kotlinx.coroutines.delay(10)
        assertNull(vm.state.value.user)

        // Login
        var loginResult: Result<User>? = null
        vm.login("another@example.com", "pwd") { loginResult = it }
        kotlinx.coroutines.delay(10)
        assertNotNull(loginResult)
        assertTrue(loginResult!!.isSuccess)
        assertEquals("another@example.com", vm.state.value.user?.email)
    }
}
