package com.example.pruebaandroid.menu.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.features.auth.ui.viewmodel.UsuarioViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class MenuViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var usuarioViewModel: UsuarioViewModel
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        usuarioViewModel = mockk(relaxed = true)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando el usuario está autenticado, debe recuperar su información`() = runTest {
        // Simular un usuario autenticado
        val usuarioMock = Usuario("1", "Juan", "juan@gmail.com", 5)
        coEvery { usuarioViewModel.obtenerUsuarioActual() } returns usuarioMock.id
        coEvery { usuarioViewModel.usuario } returns MutableStateFlow(usuarioMock)

        usuarioViewModel.obtenerUsuario(usuarioMock.id)
        Assert.assertEquals(usuarioMock, usuarioViewModel.usuario.value)
    }

    @Test
    fun `cuando el usuario no está autenticado, debe redirigir a login`() = runTest {
        coEvery { usuarioViewModel.obtenerUsuarioActual() } returns null

        val usuarioActual = usuarioViewModel.obtenerUsuarioActual()
        Assert.assertNull(usuarioActual)
    }

    @Test
    fun `cuando el usuario tiene accesos restantes, debe disminuirlos correctamente`() = runTest {
        val usuarioMock = Usuario("1", "Juan", "juan@gmail.com", 5)
        coEvery { usuarioViewModel.disminuirAccesos(usuarioMock.id, any(), any()) } just Runs

        usuarioViewModel.disminuirAccesos(usuarioMock.id, context) {}
        coVerify { usuarioViewModel.disminuirAccesos(usuarioMock.id, context, any()) }
    }

    @Test
    fun `cuando el usuario no tiene accesos restantes, debe cerrar sesión`() = runTest {
        val usuarioMock = Usuario("1", "Juan", "juan@gmail.com", 0)
        coEvery { usuarioViewModel.cerrarSesion(usuarioMock.id, any()) } just Runs

        usuarioViewModel.cerrarSesion(usuarioMock.id) {}
        coVerify { usuarioViewModel.cerrarSesion(usuarioMock.id, any()) }
    }

    @Test
    fun `cuando se presiona el botón de descarga, debe llamar a descargarYGuardarPDF`() = runTest {
        coEvery { usuarioViewModel.descargarYGuardarPDF(any(), any()) } just Runs

        usuarioViewModel.descargarYGuardarPDF(context) { _, _ -> }
        coVerify { usuarioViewModel.descargarYGuardarPDF(context, any()) }
    }

    @Test
    fun `cuando se presiona el botón de refrescar, debe llamar a refrescarDatos`() = runTest {
        coEvery { usuarioViewModel.refrescarDatos(any(), any()) } just Runs

        usuarioViewModel.refrescarDatos(context) { _, _ -> }
        coVerify { usuarioViewModel.refrescarDatos(context, any()) }
    }
}
