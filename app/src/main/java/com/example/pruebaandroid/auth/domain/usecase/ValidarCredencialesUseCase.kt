package com.example.pruebaandroid.auth.domain.usecase

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import javax.inject.Inject

class ValidarCredencialesUseCase @Inject constructor() {
    fun execute(context: Context, usuario: String, password: String, onResult: (Boolean, Int) -> Unit) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://noderedtest.coordinadora.com/api/v1/validacion-usuario/"

        val jsonRequest = JSONObject().apply {
            put("usuario", usuario)
            put("password", password)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val isError = response.optBoolean("isError", true)
                val periodoValidacion = response.optInt("perido_validacion", 0)
                onResult(!isError, periodoValidacion)
            },
            { error ->
                Log.e("API_ERROR", "Error en login: ${error.message}")
                onResult(false, 0)
            })

        requestQueue.add(request)
    }
}