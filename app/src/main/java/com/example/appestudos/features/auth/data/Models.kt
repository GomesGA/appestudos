package com.example.appestudos.features.auth.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class UsuarioRequestDTO(val nome: String, val email: String, val senha: String)
@Serializable
data class UsuarioResponseDTO(val id: Int, val nome: String, val email: String)

@Serializable
data class LoginRequestDTO(val email: String, val senha: String)
@Serializable
data class LoginResponseDTO(val usuario: UsuarioResponseDTO)

@Serializable
data class TipoPerguntaResponseDTO(val id: Int, val descricao: String)

@Serializable
data class AlterarSenhaRequestDTO(
    @SerialName("email")
    val email: String,
    @SerialName("novaSenha")
    val novaSenha: String
)

@Serializable
data class AlterarSenhaResponseDTO(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null
)

object UserManager {
    private var currentUser: UsuarioResponseDTO? = null
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "user_prefs"
    private const val KEY_USER = "current_user"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // Tenta carregar o usuário salvo ao inicializar
        loadUser()
    }

    private fun loadUser() {
        val userJson = prefs.getString(KEY_USER, null)
        if (userJson != null) {
            try {
                currentUser = Json.decodeFromString<UsuarioResponseDTO>(userJson)
            } catch (e: Exception) {
                currentUser = null
            }
        }
    }

    private fun saveUser() {
        if (currentUser != null) {
            val userJson = Json.encodeToString(currentUser)
            prefs.edit().putString(KEY_USER, userJson).apply()
        }
    }
    
    fun setCurrentUser(user: UsuarioResponseDTO) {
        currentUser = user
        saveUser()
    }
    
    fun getCurrentUser(): UsuarioResponseDTO? = currentUser
    
    fun getCurrentUserId(): Int? = currentUser?.id
    
    fun clearCurrentUser() {
        currentUser = null
        prefs.edit().remove(KEY_USER).apply()
    }
}