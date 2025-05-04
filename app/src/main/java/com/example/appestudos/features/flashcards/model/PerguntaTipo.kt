package com.example.appestudos.features.flashcards.model

data class PerguntaTipo(
    val id: Int,
    val descricao: String
)

data class PerguntaTipoResponse(
    val success: Boolean,
    val message: String?,
    val data: List<PerguntaTipo>
) 