package com.example.appestudos

import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import com.example.appestudos.navigation.AppNavigation
import com.example.appestudos.ui.theme.AppTheme
import com.example.appestudos.ui.theme.LocalThemeManager
import com.example.appestudos.ui.theme.ThemeManager
import com.google.android.libraries.places.api.Places
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.profile.data.PerformanceManager
import com.example.appestudos.features.auth.data.ApiService
import com.example.appestudos.features.auth.data.ApiClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val themeManager = ThemeManager()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o UserManager
        UserManager.init(applicationContext)
        
        // Inicializa o PerformanceManager
        PerformanceManager.init(applicationContext)

        // Validação automática do usuário salvo localmente
        val user = UserManager.getCurrentUser()
        if (user != null) {
            val apiService = ApiService(ApiClient.httpClient)
            MainScope().launch {
                try {
                    val response = apiService.validarUsuario(user.id)
                    if (!response.success) {
                        UserManager.clearCurrentUser()
                        // Opcional: você pode mostrar uma mensagem ou forçar a tela de login
                    }
                } catch (_: Exception) {
                    // Em caso de erro de rede, mantenha o usuário local
                }
            }
        }

        // Configura o callback do botão de voltar para fechar o app
        onBackPressedDispatcher.addCallback(this) {
            // Fecha o aplicativo apenas se estiver na tela inicial
            if (isTaskRoot) {
                finishAffinity()
            } else {
                isEnabled = false
                onBackPressed()
            }
        }

        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val apiKey = applicationInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""

        if (apiKey.isEmpty()) {
            throw RuntimeException("Google Maps API Key not found or empty!")
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeManager provides themeManager
            ) {
                AppTheme {
                    AppNavigation()
                }
            }
        }
    }
}