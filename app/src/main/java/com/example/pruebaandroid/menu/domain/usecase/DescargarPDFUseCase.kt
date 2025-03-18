package com.example.pruebaandroid.menu.domain.usecase

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class DescargarPDFUseCase @Inject constructor() {
    fun execute(context: Context, onResult: (Boolean, String?) -> Unit) {
        val url = "https://noderedtest.coordinadora.com/api/v1/obtenerimagen/"
        val requestQueue = Volley.newRequestQueue(context)

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val base64String = response.optString("base64", "")
                if (base64String.isNotEmpty()) {
                    val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val file = File(context.getExternalFilesDir(null), "pruebacoordi/archivo.pdf")
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { it.write(decodedBytes) }
                    onResult(true, file.absolutePath)
                } else {
                    onResult(false, null)
                }
            },
            { error ->
                Log.e("PDF_ERROR", "Error al descargar PDF: ${error.message}")
                onResult(false, null)
            })

        requestQueue.add(request)
    }
}