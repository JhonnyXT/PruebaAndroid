package com.example.pruebaandroid.splash.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pruebaandroid.auth.domain.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    val usuario = authViewModel.usuario.collectAsState().value

    LaunchedEffect(Unit) {
        authViewModel.obtenerUsuario()
        delay(2000)
        isLoading = false
    }

    LaunchedEffect(isLoading, usuario) {
        if (!isLoading) {
            if (usuario != null) {
                navController.navigate("menu") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
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
        CircularProgressIndicator(modifier = Modifier.testTag("LoaderSplash"))
    }
}
