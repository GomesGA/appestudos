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
import com.example.appestudos.features.flashcards.ui.CreateFlashcardScreen
import com.example.appestudos.features.flashcards.ui.CreateGroupScreen
import com.example.appestudos.features.flashcards.ui.FlashcardDetailScreen
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

        composable("addFlashcard") {
            CreateFlashcardScreen(navController)
        }

        composable(
            "flashcardDetail/{title}/{content}",
            arguments = listOf(
                navArgument("title"){ type = NavType.StringType },
                navArgument("content"){ type = NavType.StringType }
            )
        ) { back ->
            val t = back.arguments!!.getString("title")!!
            val c = back.arguments!!.getString("content")!!
            FlashcardDetailScreen(navController, t, c)
        }

        composable(
            route = "flashcardGroup/{groupId}/{groupName}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gid = backStackEntry.arguments?.getInt("groupId")!!
            val gname = backStackEntry.arguments?.getString("groupName")!!
            FlashcardGroupScreen(navController, gid, gname)
        }


        composable("createFlashcard") {
            CreateFlashcardScreen(navController)
        }
    }
} 