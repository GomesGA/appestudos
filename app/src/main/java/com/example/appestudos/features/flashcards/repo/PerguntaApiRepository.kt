package com.example.appestudos.features.flashcards.repo

import com.example.appestudos.features.flashcards.model.PerguntaApiModel
import com.example.appestudos.features.flashcards.model.PerguntaListResponseApiModel
import com.example.appestudos.features.flashcards.model.TipoPerguntaListResponseApiModel
import com.example.appestudos.features.auth.data.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class PerguntaApiRepository {
    suspend fun buscarPerguntas(): PerguntaListResponseApiModel {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/pergunta")
            .body()
    }

    suspend fun criarPergunta(pergunta: PerguntaApiModel) {
        ApiClient.httpClient.post("${ApiClient.BASE_URL}/pergunta") {
            contentType(ContentType.Application.Json)
            setBody(pergunta)
        }
    }

    suspend fun buscarTiposPergunta(): TipoPerguntaListResponseApiModel {
        return ApiClient.httpClient.get("${ApiClient.BASE_URL}/pergunta/tipos")
            .body()
    }
} 