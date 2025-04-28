package com.example.appestudos.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appestudos.features.auth.ui.ChangePasswordScreen
import com.example.appestudos.features.auth.ui.LoginScreen
import com.example.appestudos.features.auth.ui.RegisterScreen
import com.example.appestudos.features.flashcards.ui.CreateGroupScreen
import com.example.appestudos.features.flashcards.ui.FlashcardGroupScreen
import com.example.appestudos.features.intro.ui.HomeScreen
import com.example.appestudos.features.map.ui.MapScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "LoginScreen") {
        composable("LoginScreen") {
            LoginScreen(navController)
        }
        composable("RegisterScreen") {
            RegisterScreen(navController)
        }
        composable("ChangePassword") {
            ChangePasswordScreen(navController)
        }
        composable("HomeScreen") {
            HomeScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }

        composable("createGroup") {
            CreateGroupScreen(navController)
        }
        composable(
            "flashcardGroup/{groupId}/{groupName}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
            FlashcardGroupScreen(navController, groupId, groupName)
        }
    }
} 