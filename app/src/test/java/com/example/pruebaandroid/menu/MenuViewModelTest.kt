package com.example.pruebaandroid.menu.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.domain.AuthViewModel
import com.example.pruebaandroid.auth.data.model.Usuario
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
class MenuViewModelTest {

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
    fun `cuando el usuario tiene accesos, debe disminuirlos`() = runTest {
        val usuarioFake = Usuario("1", "Pedro", "pedro@gmail.com", 3)
        every { authViewModel.usuario } returns MutableStateFlow(usuarioFake)

        authViewModel.disminuirAccesos(usuarioFake.id, mockk(relaxed = true), {})

        advanceUntilIdle() // Esperar a que termine la corutina
        verify { authViewModel.disminuirAccesos(usuarioFake.id, any(), any()) }
    }

    @Test
    fun `cuando el usuario no tiene accesos, debe cerrar sesión`() = runTest {
        val usuarioFake = Usuario("1", "Ana", "ana@gmail.com", 0)
        every { authViewModel.usuario } returns MutableStateFlow(usuarioFake)

        authViewModel.cerrarSesion(usuarioFake.id, {})

        advanceUntilIdle() // Esperar a que termine la corutina
        verify { authViewModel.cerrarSesion(usuarioFake.id, any()) }
    }
}
