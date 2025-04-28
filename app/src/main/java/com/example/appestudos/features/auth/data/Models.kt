package com.example.appestudos.features.auth.data

import kotlinx.serialization.Serializable

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
data class AlterarSenhaRequestDTO(val email: String, val novaSenha: String)

object UserManager {
    private var currentUser: UsuarioResponseDTO? = null
    
    fun setCurrentUser(user: UsuarioResponseDTO) {
        currentUser = user
    }
    
    fun getCurrentUser(): UsuarioResponseDTO? = currentUser
    
    fun getCurrentUserId(): Int? = currentUser?.id
    
    fun clearCurrentUser() {
        currentUser = null
    }
}