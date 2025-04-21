package com.example.appestudos

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            login(navController)
        }
        composable("cadastro") {
            cadastro(navController)
        }
        composable("esenha") {
            esenha(navController)
        }
        composable("intro") {
            intro(navController)
        }
    }
} 