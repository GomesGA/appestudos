package com.example.appestudos.features.auth.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val client: HttpClient) {
    suspend fun registrar(usuario: UsuarioRequestDTO): ApiResponse<UsuarioResponseDTO> =
        client.post("${ApiClient.BASE_URL}/usuarios/registrar") {
            contentType(ContentType.Application.Json)
            setBody(usuario)
        }.body()

    suspend fun login(request: LoginRequestDTO): ApiResponse<LoginResponseDTO> =
        client.post("${ApiClient.BASE_URL}/usuarios/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun listarTiposPergunta(): ApiResponse<List<TipoPerguntaResponseDTO>> =
        client.get("${ApiClient.BASE_URL}/pergunta/tipos").body()

    suspend fun alterarSenha(request: AlterarSenhaRequestDTO): ApiResponse<AlterarSenhaResponseDTO> =
        client.put("${ApiClient.BASE_URL}/usuarios/atualizar-senha") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}