package com.example.appestudos.features.flashcards.model

import kotlinx.serialization.Serializable

@Serializable
data class GrupoRequestDTO(
    val descricao: String,
    val imagemPath: String,
    val usuarioId: Int
) 