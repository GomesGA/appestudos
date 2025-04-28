package com.example.appestudos.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlterarSenhaRequestDTO(
    @SerialName("email")
    val email: String,
    @SerialName("novaSenha")
    val novaSenha: String
) 