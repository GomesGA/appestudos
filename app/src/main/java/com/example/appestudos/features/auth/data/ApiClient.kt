package com.example.appestudos.features.auth.data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    // Base URL do seu backend. Se estiver rodando no emulador Android, use 10.0.2.2
    const val BASE_URL = "http://10.0.2.2:8080/api"

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY    // retire ou mude para NONE em produção
        }
    }
}