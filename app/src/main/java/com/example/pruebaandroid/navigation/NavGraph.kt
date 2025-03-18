package com.example.pruebaandroid.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pruebaandroid.splash.ui.SplashScreen
import com.example.pruebaandroid.auth.ui.LoginScreen
import com.example.pruebaandroid.menu.ui.MenuScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("menu") { MenuScreen(navController) }
    }
}
