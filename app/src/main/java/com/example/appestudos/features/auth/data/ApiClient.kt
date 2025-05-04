package com.example.appestudos.features.auth.data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    // Antes (emulador):
    //const val BASE_URL = "http://10.0.2.2:8080/api"

    // Para dispositivo real com adb reverse:
    const val BASE_URL = "http://localhost:8080/api"
    // ou
    // const val BASE_URL = "http://127.0.0.1:8080/api"

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }
}