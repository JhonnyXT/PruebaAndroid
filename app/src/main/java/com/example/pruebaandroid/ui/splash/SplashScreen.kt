package com.example.pruebaandroid.ui.splash

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pruebaandroid.ui.viewmodel.UsuarioViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, usuarioViewModel: UsuarioViewModel = hiltViewModel()) {
    var isLoading by remember { mutableStateOf(true) }
    val usuario = usuarioViewModel.usuario.collectAsState().value

    LaunchedEffect(Unit) {
        usuarioViewModel.obtenerUsuarioDesdeRoom()
        delay(2000)
        isLoading = false
    }

    LaunchedEffect(isLoading, usuario) {
        if (!isLoading) {
            Log.d("DEBUG_SPLASH", "Usuario en Room: $usuario")
            if (usuario != null) {
                navController.navigate("menu") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                Log.d("DEBUG_SPLASH", "No se encontró usuario, redirigiendo a Login")
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
