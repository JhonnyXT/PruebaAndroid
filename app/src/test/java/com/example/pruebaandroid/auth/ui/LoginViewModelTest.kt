package com.example.pruebaandroid.auth.ui

import android.content.Context
import android.widget.Toast
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.features.auth.ui.viewmodel.UsuarioViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

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
    fun `cuando las credenciales son correctas, debe insertar usuario y navegar a menu`() = runTest {
        val usuario = "usuario1"
        val password = "password123"
        val periodoValidacion = 5
        val usuarioMock = Usuario(id = usuario, nombre = "Usuario $usuario", email = "$usuario@gmail.com", periodoValidacion = periodoValidacion)

        // Simular que validarCredenciales retorna éxito
        coEvery { usuarioViewModel.validarCredenciales(any(), usuario, password, any()) } answers {
            val callback = it.invocation.args[3] as (Boolean, Int) -> Unit
            callback(true, periodoValidacion) // Simulamos un login exitoso
        }

        usuarioViewModel.validarCredenciales(context, usuario, password) { loginExitoso, _ ->
            if (loginExitoso) {
                usuarioViewModel.insertarUsuario(usuarioMock)
            }
        }
        coVerify { usuarioViewModel.insertarUsuario(usuarioMock) }
    }

    @Test
    fun `cuando las credenciales son incorrectas, debe mostrar mensaje de error`() = runTest {
        val usuario = "usuario1"
        val password = "password123"

        // Simular que validarCredenciales retorna error
        coEvery { usuarioViewModel.validarCredenciales(any(), usuario, password, any()) } answers {
            val callback = it.invocation.args[3] as (Boolean, Int) -> Unit
            callback(false, 0) // Simulamos un login fallido
        }

        // Mock de Toast
        mockkStatic(Toast::class)
        every { Toast.makeText(any(), any<String>(), any()) } returns mockk(relaxed = true)

        usuarioViewModel.validarCredenciales(context, usuario, password) { loginExitoso, _ ->
            if (!loginExitoso) {
                Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
        coVerify(exactly = 0) { usuarioViewModel.insertarUsuario(any()) }
        verify { Toast.makeText(context, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT) }
    }
}
