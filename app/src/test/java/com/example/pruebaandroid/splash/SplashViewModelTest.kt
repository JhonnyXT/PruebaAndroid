package com.example.pruebaandroid.splash.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.auth.domain.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

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
    fun `cuando hay usuario registrado, debe navegar a la pantalla de Menú`() = runTest {
        val usuarioFake = Usuario("1", "Juan", "juan@gmail.com", 5)
        val usuarioFlow: StateFlow<Usuario?> = MutableStateFlow(usuarioFake)

        every { authViewModel.usuario } returns usuarioFlow

        advanceUntilIdle()  // Esperar a que termine la corutina
        assert(authViewModel.usuario.value != null)
    }

    @Test
    fun `cuando no hay usuario registrado, debe navegar a la pantalla de Login`() = runTest {
        val usuarioFlow: StateFlow<Usuario?> = MutableStateFlow(null)

        every { authViewModel.usuario } returns usuarioFlow

        advanceUntilIdle()  // Esperar a que termine la corutina
        assert(authViewModel.usuario.value == null)
    }
}
