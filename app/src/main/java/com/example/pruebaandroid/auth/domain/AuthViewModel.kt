package com.example.pruebaandroid.auth.domain

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaandroid.auth.data.model.Usuario
import com.example.pruebaandroid.auth.domain.usecase.DisminuirAccesosUseCase
import com.example.pruebaandroid.auth.domain.usecase.EliminarUsuarioUseCase
import com.example.pruebaandroid.auth.domain.usecase.InsertarUsuarioUseCase
import com.example.pruebaandroid.auth.domain.usecase.ObtenerUsuarioUseCase
import com.example.pruebaandroid.auth.domain.usecase.ValidarCredencialesUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val insertarUsuarioUseCase: InsertarUsuarioUseCase,
    private val disminuirAccesosUseCase: DisminuirAccesosUseCase,
    private val eliminarUsuarioUseCase: EliminarUsuarioUseCase,
    private val validarCredencialesUseCase: ValidarCredencialesUseCase
) : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun obtenerUsuario() {
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

    fun validarCredenciales(
        context: Context,
        usuario: String,
        password: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        validarCredencialesUseCase.execute(context, usuario, password, onResult)
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

    fun cerrarSesion(userId: String, onLogout: () -> Unit) {
        viewModelScope.launch {
            eliminarUsuario(userId, onLogout)

            firestore.collection("usuario_login").document(userId).delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Usuario eliminado de Firestore")
                    onLogout()
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Error al eliminar usuario: ${it.message}")
                }
        }
    }

    private fun eliminarUsuario(userId: String, onLogout: () -> Unit) {
        viewModelScope.launch {
            eliminarUsuarioUseCase(userId)
            firestore.collection("usuario_login").document(userId).delete()
            onLogout()
        }
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
            .addOnSuccessListener { 
                Log.d("Firestore", "Usuario guardado en Firestore")
            }
            .addOnFailureListener { e -> 
                Log.e("Firestore", "Error al guardar usuario: ${e.message}")
                // Si falla el guardado en Firestore, al menos mantenemos el usuario en la base de datos local
            }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            authResult.user?.let { firebaseUser ->
                val nuevoUsuario = Usuario(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName ?: "Usuario Google",
                    email = firebaseUser.email ?: "",
                    periodoValidacion = 5
                )

                insertarUsuario(nuevoUsuario)

                try {
                    guardarUsuarioEnFirestore(nuevoUsuario)
                } catch (e: Exception) {
                    Log.e("Firestore", "Error al guardar en Firestore: ${e.message}")
                }
                
                onSuccess()
            } ?: run {
                onError("Error al obtener información del usuario")
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error en la autenticación con Google: ${e.message}")
            Log.e("GoogleSignIn", "Stack trace: ${e.stackTraceToString()}")
            when (e) {
                is com.google.firebase.auth.FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_INVALID_CREDENTIAL" -> onError("Credenciales inválidas")
                        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> onError("La cuenta ya existe con diferentes credenciales")
                        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> onError("Las credenciales ya están en uso")
                        else -> onError("Error de autenticación: ${e.errorCode}")
                    }
                }
                else -> onError("Error desconocido: ${e.message}")
            }
        }
    }
}