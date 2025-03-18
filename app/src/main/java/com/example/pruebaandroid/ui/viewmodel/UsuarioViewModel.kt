package com.example.pruebaandroid.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pruebaandroid.data.model.Usuario
import com.example.pruebaandroid.data.repository.UsuarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun obtenerUsuario(id: String) {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuario(id)
            Log.d("DEBUG_ROOM", "Usuario obtenido: $usuario")
            _usuario.value = usuario
        }
    }

    fun obtenerUsuarioDesdeRoom() {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerPrimerUsuario()
            Log.d("DEBUG_OBTENER", "Usuario obtenido desde Room: $usuario")
            _usuario.value = usuario
        }
    }

    suspend fun obtenerUsuarioActual(): String? {
        return usuarioRepository.obtenerPrimerUsuario()?.id
    }

    fun insertarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            Log.d("DEBUG_INSERTAR", "Intentando guardar usuario en Room: $usuario")
            usuarioRepository.insertarUsuario(usuario)

            // Verifica si se guardó correctamente
            val usuarioGuardado = usuarioRepository.obtenerUsuario(usuario.id)
            Log.d("DEBUG_INSERTAR", "Usuario guardado en Room: $usuarioGuardado")

            _usuario.value = usuarioGuardado
            guardarUsuarioEnFirestore(usuario)
        }
    }


    // ✅ Disminuir accesos en Room y sincronizar con Firestore
    fun disminuirAccesos(userId: String, context: Context, onLogout: () -> Unit) {
        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.obtenerUsuario(userId)
                Log.d("DEBUG_DISMINUIR", "Usuario obtenido antes de disminuir: $usuario")

                usuario?.let {
                    if (it.periodoValidacion > 0) {
                        val filasAfectadas = usuarioRepository.disminuirAccesos(it.id)
                        Log.d("DEBUG_DISMINUIR", "Filas afectadas en Room: $filasAfectadas")

                        if (filasAfectadas > 0) { // 🔹 Asegura que la BD se actualizó
                            val nuevoPeriodo = it.periodoValidacion - 1
                            val usuarioActualizado = it.copy(periodoValidacion = nuevoPeriodo)
                            _usuario.value = usuarioActualizado

                            // Sincronizar con Firestore si hay Internet
                            if (isInternetAvailable(context)) {
                                firestore.collection("usuario_login").document(userId)
                                    .update("periodo_validacion", nuevoPeriodo)
                                    .addOnFailureListener {
                                        Log.e("Firestore", "No se pudo sincronizar con Firestore.")
                                    }
                            }

                            if (nuevoPeriodo == 0) {
                                Toast.makeText(context, "Accesos agotados. Se cerrará la sesión.", Toast.LENGTH_LONG).show()
                                cerrarSesion(userId, onLogout)
                            }
                        } else {
                            Log.e("DEBUG_DISMINUIR", "No se afectaron filas en la BD")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UsuarioViewModel", "Error al disminuir accesos: ${e.message}")
            }
        }
    }

    // ✅ Eliminar usuario de Room y Firestore
    fun eliminarUsuario(id: String) {
        viewModelScope.launch {
            usuarioRepository.eliminarUsuario(id)
            firestore.collection("usuario_login").document(id).delete()
                .addOnSuccessListener { Log.d("Firestore", "Usuario eliminado de Firestore") }
                .addOnFailureListener { Log.e("Firestore", "Error al eliminar usuario: ${it.message}") }
        }
    }

    // ✅ Validar credenciales y guardar usuario en Room y Firestore solo si es nuevo
    fun validarCredenciales(context: Context, usuario: String, password: String, onResult: (Boolean, Int) -> Unit) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://noderedtest.coordinadora.com/api/v1/validacion-usuario/"

        val jsonRequest = JSONObject().apply {
            put("usuario", usuario)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonRequest,
            { response ->
                val isError = response.optBoolean("isError", true)
                val periodoValidacion = response.optInt("perido_validacion", 0)

                if (!isError) {
                    viewModelScope.launch {
                        val usuarioExistente = usuarioRepository.obtenerUsuario(usuario)
                        if (usuarioExistente == null) {
                            val nuevoUsuario = Usuario(
                                id = usuario,
                                nombre = "Usuario $usuario",
                                email = "$usuario@gmail.com",
                                periodoValidacion = periodoValidacion
                            )
                            insertarUsuario(nuevoUsuario)
                        }
                    }
                    onResult(true, periodoValidacion)
                } else {
                    onResult(false, 0)
                }
            },
            { error ->
                Log.e("API_ERROR", "Error en login: ${error.message}")
                onResult(false, 0)
            }
        )

        requestQueue.add(request)
    }

    // ✅ Guardar usuario en Firestore
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
            .addOnSuccessListener { Log.d("Firestore", "Usuario guardado correctamente en Firestore") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error al guardar usuario en Firestore: ${e.message}") }
    }

    // ✅ Verificar si hay conexión a Internet
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun cerrarSesion(userId: String, onLogout: () -> Unit) {
        viewModelScope.launch {
            eliminarUsuario(userId) // Eliminar de Room

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

    fun descargarYGuardarPDF(context: Context, onResult: (Boolean, String?) -> Unit) {
        val url = "https://noderedtest.coordinadora.com/api/v1/obtenerimagen/"

        val requestQueue = Volley.newRequestQueue(context)

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("PDF_RESPONSE", "Respuesta completa de la API: $response")

                try {
                    // Extraer base64
                    val base64String = response.optString("base64", "")

                    if (base64String.isNotEmpty()) {
                        Log.d("PDF_BASE64", "Base64 recibido correctamente, longitud: ${base64String.length}")

                        // Decodificar Base64
                        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

                        // Guardar el archivo en la carpeta de Descargas
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val pdfFile = File(downloadsDir, "pruebacoordi/archivo.pdf")

                        pdfFile.parentFile?.mkdirs()

                        // Escribir el archivo
                        FileOutputStream(pdfFile).use { fos ->
                            fos.write(decodedBytes)
                        }

                        Log.d("PDF_SAVED", "Archivo guardado en: ${pdfFile.absolutePath}")
                        onResult(true, pdfFile.absolutePath)
                    } else {
                        Log.e("PDF_ERROR", "El campo 'base64' está vacío o no existe")
                        onResult(false, null)
                    }
                } catch (e: Exception) {
                    Log.e("PDF_ERROR", "Error al guardar el archivo: ${e.message}")
                    onResult(false, null)
                }
            },
            { error ->
                Log.e("PDF_ERROR", "Error en la solicitud: ${error.message}")
                onResult(false, null)
            }
        )

        requestQueue.add(jsonRequest)
    }

    fun refrescarDatos(context: Context, onResult: (Boolean, String?) -> Unit) {
        val url = "https://noderedtest.coordinadora.com/api/v1/obtenerimagen/"

        val requestQueue = Volley.newRequestQueue(context)

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("REFRESH_RESPONSE", "Respuesta completa de la API: $response")

                try {
                    // Extraer base64 y periodo_validacion
                    val base64String = response.optString("base64", "")
                    val nuevoPeriodoValidacion = response.optInt("perido_validacion", -1)

                    viewModelScope.launch {
                        usuario.value?.let { usuarioExistente ->
                            if (nuevoPeriodoValidacion != -1) {
                                // Crear una copia del usuario con el nuevo periodo de validación
                                val updatedUser = usuarioExistente.copy(periodoValidacion = nuevoPeriodoValidacion)

                                // Guardar en Room Database
                                insertarUsuario(updatedUser)

                                // Guardar en Firestore
                                guardarUsuarioEnFirestore(updatedUser)

                                // Actualizar en el StateFlow
                                _usuario.value = updatedUser
                            }
                        }
                    }

                    if (base64String.isNotEmpty()) {
                        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val pdfFile = File(downloadsDir, "pruebacoordi/archivo.pdf")

                        pdfFile.parentFile?.mkdirs()

                        FileOutputStream(pdfFile).use { fos ->
                            fos.write(decodedBytes)
                        }

                        Log.d("REFRESH_SAVED", "Archivo actualizado en: ${pdfFile.absolutePath}")
                        onResult(true, pdfFile.absolutePath)
                    } else {
                        onResult(false, null)
                    }
                } catch (e: Exception) {
                    Log.e("REFRESH_ERROR", "Error al actualizar datos: ${e.message}")
                    onResult(false, null)
                }
            },
            { error ->
                Log.e("REFRESH_ERROR", "Error en la solicitud: ${error.message}")
                onResult(false, null)
            }
        )

        requestQueue.add(jsonRequest)
    }
}