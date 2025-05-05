package com.example.appestudos.features.flashcards.model

import kotlinx.serialization.Serializable

@Serializable
data class AlternativaApiModel(
    val id: Int? = null,
    val descricao: String,
    val correta: Boolean
)

@Serializable
data class RespostaApiModel(
    val alternativas: List<AlternativaApiModel>? = null,
    val texto: String? = null,
    val numero: Int? = null,
    val booleano: Boolean? = null
)

@Serializable
data class PerguntaApiModel(
    val id: Int? = null,
    val idTipo: Int? = null,
    val idUsuario: Int? = null,
    val idGrupo: Int? = null,
    val pergunta: String,
    val privada: Boolean,
    val resposta: RespostaApiModel? = null
)

@Serializable
data class PerguntaResponseApiModel(
    val id: Int,
    val idUsuario: Int,
    val idGrupo: Int? = null,
    val descricao: String? = null,
    val gabaritoTexto: String? = null,
    val gabaritoNumero: Int? = null,
    val gabaritoBooleano: Boolean? = null,
    val alternativas: List<AlternativaApiModel>? = null
)

@Serializable
data class PerguntaListResponseApiModel(
    val success: Boolean,
    val message: String?,
    val data: List<PerguntaResponseApiModel>
)

@Serializable
data class PerguntaDeleteDTO(
    val id: Int,
    val idUsuario: Int
)