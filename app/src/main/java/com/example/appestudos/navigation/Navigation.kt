package com.example.appestudos.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.profile.presentation.PerformanceScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Verifica se existe um usuário logado e redireciona se necessário
    LaunchedEffect(Unit) {
        if (UserManager.getCurrentUser() != null) {
            navController.navigate("HomeScreen") {
                popUpTo("LoginScreen") { inclusive = true }
            }
        }
    }

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
        composable("performance") {
            PerformanceScreen(navController)
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
            route = "flashcardGroup/{groupId}/{groupName}/{isPrivateParam}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType },
                navArgument("groupName") { type = NavType.StringType },
                navArgument("isPrivateParam") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gid = backStackEntry.arguments?.getInt("groupId")!!
            val gname = backStackEntry.arguments?.getString("groupName")!!
            val isPrivateParam = backStackEntry.arguments?.getString("isPrivateParam") ?: "public"
            FlashcardGroupScreen(navController, gid, gname, isPrivateParam)
        }

        composable("createFlashcard") {
            CreateFlashcardScreen(navController)
        }
    }
} 