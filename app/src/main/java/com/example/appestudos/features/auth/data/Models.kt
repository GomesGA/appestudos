package com.example.appestudos.features.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

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