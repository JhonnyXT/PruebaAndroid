package com.example.pruebaandroid.auth.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.auth.domain.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)  // Configurar dispatcher de prueba
        authViewModel = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // Restaurar dispatcher original
    }

    @Test
    fun `cuando hay usuario registrado, debe obtenerlo`() = runTest {
        val usuarioFake = Usuario("1", "Juan", "juan@gmail.com", 5)
        every { authViewModel.usuario } returns MutableStateFlow(usuarioFake)

        authViewModel.obtenerUsuario()

        advanceUntilIdle() // Esperar a que termine la corutina
        assert(authViewModel.usuario.value == usuarioFake)
    }

    @Test
    fun `cuando no hay usuario registrado, usuario debe ser null`() = runTest {
        every { authViewModel.usuario } returns MutableStateFlow(null)

        authViewModel.obtenerUsuario()

        advanceUntilIdle() // Esperar a que termine la corutina
        assert(authViewModel.usuario.value == null)
    }
}
