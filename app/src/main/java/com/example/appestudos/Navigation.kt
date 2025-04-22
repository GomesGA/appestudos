package com.example.appestudos

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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