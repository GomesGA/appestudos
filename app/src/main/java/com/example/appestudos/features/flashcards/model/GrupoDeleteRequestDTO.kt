package com.example.appestudos.features.flashcards.model

import kotlinx.serialization.Serializable

@Serializable
data class GrupoDeleteRequestDTO(
    val id: Int,
    val usuarioId: Int
) 