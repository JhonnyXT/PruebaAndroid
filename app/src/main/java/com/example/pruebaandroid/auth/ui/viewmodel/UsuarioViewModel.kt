package com.example.pruebaandroid.features.auth.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.auth.domain.usecase.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val insertarUsuarioUseCase: InsertarUsuarioUseCase,
    private val disminuirAccesosUseCase: DisminuirAccesosUseCase,
    private val eliminarUsuarioUseCase: EliminarUsuarioUseCase,
    private val validarCredencialesUseCase: ValidarCredencialesUseCase,
    private val descargarPDFUseCase: DescargarPDFUseCase,
    private val refrescarDatosUseCase: RefrescarDatosUseCase
) : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun obtenerUsuario(id: String) {
        viewModelScope.launch {
            _usuario.value = obtenerUsuarioUseCase(id)
        }
    }

    fun obtenerUsuarioDesdeRoom() {
        viewModelScope.launch {
            _usuario.value = obtenerUsuarioUseCase(null)
        }
    }

    suspend fun obtenerUsuarioActual(): String? {
        return obtenerUsuarioUseCase(null)?.id
    }

    fun insertarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            insertarUsuarioUseCase(usuario)
            _usuario.value = usuario
            guardarUsuarioEnFirestore(usuario)
        }
    }

    fun disminuirAccesos(userId: String, context: Context, onLogout: () -> Unit) {
        viewModelScope.launch {
            val usuario = obtenerUsuarioUseCase(userId)
            usuario?.let {
                if (it.periodoValidacion > 0) {
                    val filasAfectadas = disminuirAccesosUseCase(it.id)
                    if (filasAfectadas > 0) {
                        val nuevoPeriodo = it.periodoValidacion - 1
                        val usuarioActualizado = it.copy(periodoValidacion = nuevoPeriodo)
                        _usuario.value = usuarioActualizado

                        if (isInternetAvailable(context)) {
                            firestore.collection("usuario_login").document(userId)
                                .update("periodo_validacion", nuevoPeriodo)
                        }

                        if (nuevoPeriodo == 0) {
                            Toast.makeText(context, "Accesos agotados. Cerrando sesión...", Toast.LENGTH_SHORT).show()
                            eliminarUsuario(userId, onLogout)
                        }
                    }
                }
            }
        }
    }

    fun eliminarUsuario(userId: String, onLogout: () -> Unit) {
        viewModelScope.launch {
            eliminarUsuarioUseCase(userId)
            firestore.collection("usuario_login").document(userId).delete()
            onLogout()
        }
    }

    fun validarCredenciales(context: Context, usuario: String, password: String, onResult: (Boolean, Int) -> Unit) {
        validarCredencialesUseCase.execute(context, usuario, password, onResult)
    }

    fun descargarYGuardarPDF(context: Context, onResult: (Boolean, String?) -> Unit) {
        descargarPDFUseCase.execute(context, onResult)
    }

    fun refrescarDatos(context: Context, onResult: (Boolean, String?) -> Unit) {
        refrescarDatosUseCase.execute(context, onResult)
    }

    private fun guardarUsuarioEnFirestore(usuario: Usuario) {
        val usuarioData = hashMapOf(
            "id" to usuario.id,
            "nombre" to usuario.nombre,
            "email" to usuario.email,
            "periodo_validacion" to usuario.periodoValidacion,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("usuario_login").document(usuario.id)
            .set(usuarioData)
            .addOnSuccessListener { Log.d("Firestore", "Usuario guardado en Firestore") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error al guardar usuario: ${e.message}") }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun cerrarSesion(userId: String, onLogout: () -> Unit) {
        viewModelScope.launch {
            eliminarUsuario(userId, onLogout) // Eliminar de Room

            firestore.collection("usuario_login").document(userId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Usuario eliminado de Firestore")
                    onLogout() // Redirigir al Login
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Error al eliminar usuario: ${it.message}")
                }
        }
    }
}
