package com.example.appestudos.features.flashcards.model

import kotlinx.serialization.Serializable

@Serializable
data class GrupoResponseDTO(
    val id: Int,
    val descricao: String,
    val path: String,
    val idUsuario: Int? // Pode ser null para público
) 