package com.example.appestudos.features.flashcards.model

import kotlinx.serialization.Serializable

@Serializable
data class TipoPerguntaApiModel(
    val id: Int,
    val descricao: String
)

@Serializable
data class TipoPerguntaListResponseApiModel(
    val success: Boolean,
    val message: String?,
    val data: List<TipoPerguntaApiModel>
) 