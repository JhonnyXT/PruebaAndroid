package com.example.pruebaandroid.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pruebaandroid.splash.ui.SplashScreen
import com.example.pruebaandroid.auth.ui.LoginScreen
import com.example.pruebaandroid.auth.domain.AuthViewModel
import com.example.pruebaandroid.menu.ui.MenuScreen
import com.example.pruebaandroid.menu.domain.MenuViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            val authViewModel = hiltViewModel<AuthViewModel>()
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("login") {
            val authViewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable("menu") {
            val authViewModel = hiltViewModel<AuthViewModel>()
            val menuViewModel = hiltViewModel<MenuViewModel>()
            MenuScreen(
                navController = navController,
                authViewModel = authViewModel,
                menuViewModel = menuViewModel
            )
        }
    }
}
