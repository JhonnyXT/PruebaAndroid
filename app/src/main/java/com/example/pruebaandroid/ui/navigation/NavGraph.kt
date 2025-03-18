package com.example.pruebaandroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pruebaandroid.ui.splash.SplashScreen
import com.example.pruebaandroid.ui.login.LoginScreen
import com.example.pruebaandroid.ui.menu.MenuScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController, hiltViewModel()) }
        composable("login") { LoginScreen(navController) }
        composable("menu") { MenuScreen(navController) }
    }
}
