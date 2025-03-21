package com.example.pruebaandroid.splash.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.data.model.Usuario
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    // Reglas necesarias para pruebas con LiveData y coroutines
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var usuarioViewModel: UsuarioViewModel

    @Before
    fun setup() {
        usuarioViewModel = mockk(relaxed = true)
    }

    @Test
    fun `cuando hay usuario registrado, debe navegar a la pantalla de Menú`() = runTest {
        // Simular un usuario existente en Room
        val usuarioFake = Usuario("1", "Juan", "juan@gmail.com", 5)
        every { usuarioViewModel.usuario } returns MutableStateFlow(usuarioFake)

        usuarioViewModel.obtenerUsuarioDesdeRoom()

        verify { usuarioViewModel.obtenerUsuarioDesdeRoom() }
        assert(usuarioViewModel.usuario.value != null)
    }

    @Test
    fun `cuando no hay usuario registrado, debe navegar a la pantalla de Login`() = runTest {
        // Simular que no hay usuario en Room
        every { usuarioViewModel.usuario } returns MutableStateFlow(null)

        usuarioViewModel.obtenerUsuarioDesdeRoom()

        verify { usuarioViewModel.obtenerUsuarioDesdeRoom() }
        assert(usuarioViewModel.usuario.value == null)
    }
}
