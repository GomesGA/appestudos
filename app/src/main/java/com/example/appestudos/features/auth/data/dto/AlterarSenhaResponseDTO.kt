package com.example.appestudos.features.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlterarSenhaResponseDTO(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null
) 