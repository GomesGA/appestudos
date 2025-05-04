package com.example.appestudos.features.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appestudos.features.flashcards.model.PerguntaApiModel
import com.example.appestudos.features.flashcards.model.PerguntaListResponseApiModel
import com.example.appestudos.features.flashcards.model.TipoPerguntaApiModel
import com.example.appestudos.features.flashcards.repo.PerguntaApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class FlashcardViewModel : ViewModel() {
    private val repo = PerguntaApiRepository()
    private val _perguntas = MutableStateFlow<List<com.example.appestudos.features.flashcards.model.PerguntaResponseApiModel>>(emptyList())
    val perguntas: StateFlow<List<com.example.appestudos.features.flashcards.model.PerguntaResponseApiModel>> = _perguntas

    private val _tiposPergunta = MutableStateFlow<List<TipoPerguntaApiModel>>(emptyList())
    val tiposPergunta: StateFlow<List<TipoPerguntaApiModel>> = _tiposPergunta

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarPerguntas() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = repo.buscarPerguntas()
                _perguntas.value = response.data
            } catch (e: Exception) {
                _error.value = "Erro ao carregar perguntas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarTiposPergunta() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("FlashcardViewModel", "Iniciando carregamento de tipos de pergunta")
                val response = repo.buscarTiposPergunta()
                Log.d("FlashcardViewModel", "Resposta da API: $response")
                _tiposPergunta.value = response.data
                Log.d("FlashcardViewModel", "Tipos de pergunta carregados: ${response.data}")
            } catch (e: Exception) {
                Log.e("FlashcardViewModel", "Erro ao carregar tipos de pergunta", e)
                _error.value = "Erro ao carregar tipos de pergunta: ${e.message}"
                // Em caso de erro, carrega uma lista vazia para evitar crash
                _tiposPergunta.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun criarPergunta(pergunta: PerguntaApiModel, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repo.criarPergunta(pergunta)
                onSuccess()
                carregarPerguntas() // Atualiza lista após criar
            } catch (e: Exception) {
                _error.value = "Erro ao criar pergunta: ${e.message}"
                onError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPublicas() = perguntas.value.filter { it.gabaritoTexto != null && it.gabaritoTexto != "" && it.gabaritoTexto != "true" && it.gabaritoTexto != "false" }
    fun getPrivadas() = perguntas.value.filter { it.gabaritoTexto == "true" || it.gabaritoTexto == "false" }
}