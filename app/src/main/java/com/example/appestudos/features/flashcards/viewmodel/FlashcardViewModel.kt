package com.example.appestudos.features.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appestudos.features.flashcards.model.*
import com.example.appestudos.features.flashcards.repo.PerguntaApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow

class FlashcardViewModel : ViewModel() {
    private val repo = PerguntaApiRepository()
    private val _perguntas = MutableStateFlow<List<PerguntaResponseApiModel>>(emptyList())
    val perguntas: StateFlow<List<PerguntaResponseApiModel>> = _perguntas

    private val _tiposPergunta = MutableStateFlow<List<PerguntaTipo>>(emptyList())
    val tiposPergunta: StateFlow<List<PerguntaTipo>> = _tiposPergunta.asStateFlow()

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
                _tiposPergunta.value = response.data.map { PerguntaTipo(it.id, it.descricao) }
                Log.d("FlashcardViewModel", "Tipos de pergunta carregados: ${response.data}")
            } catch (e: Exception) {
                Log.e("FlashcardViewModel", "Erro ao carregar tipos de pergunta", e)
                _error.value = "Erro ao carregar tipos de pergunta: ${e.message}"
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

    fun criarGrupo(descricao: String, imagemPath: String, usuarioId: Int, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = GrupoRequestDTO(
                    descricao = descricao,
                    imagemPath = imagemPath,
                    usuarioId = usuarioId
                )
                repo.criarGrupo(request)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao criar grupo: ${e.message}"
                onError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletarGrupo(grupoId: Int, usuarioId: Int, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val request = GrupoDeleteRequestDTO(
                    id = grupoId,
                    usuarioId = usuarioId
                )
                repo.deletarGrupo(request)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao deletar grupo: ${e.message}"
                onError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPublicas() = perguntas.value.filter { it.gabaritoTexto != null && it.gabaritoTexto != "" && it.gabaritoTexto != "true" && it.gabaritoTexto != "false" }
    fun getPrivadas() = perguntas.value.filter { it.gabaritoTexto == "true" || it.gabaritoTexto == "false" }

    fun criarPerguntaMultiplaEscolha(
        idUsuario: Int,
        idGrupo: Int,
        pergunta: String,
        privada: Boolean,
        alternativas: List<String>,
        corretas: List<Boolean>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val resposta = RespostaApiModel(
                    alternativas = alternativas.mapIndexed { index, descricao ->
                        AlternativaApiModel(descricao = descricao, correta = corretas[index])
                    },
                    texto = null,
                    numero = null,
                    booleano = null
                )

                val perguntaModel = PerguntaApiModel(
                    idTipo = 1,
                    idUsuario = idUsuario,
                    idGrupo = idGrupo,
                    pergunta = pergunta,
                    privada = privada,
                    resposta = resposta
                )

                repo.criarPergunta(perguntaModel)
                carregarPerguntas()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao criar pergunta: ${e.message}"
            }
        }
    }

    fun criarPerguntaNumerica(
        idUsuario: Int,
        idGrupo: Int,
        pergunta: String,
        privada: Boolean,
        resposta: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respostaModel = RespostaApiModel(
                    alternativas = emptyList(),
                    texto = null,
                    numero = resposta,
                    booleano = null
                )

                val perguntaModel = PerguntaApiModel(
                    idTipo = 2,
                    idUsuario = idUsuario,
                    idGrupo = idGrupo,
                    pergunta = pergunta,
                    privada = privada,
                    resposta = respostaModel
                )

                repo.criarPergunta(perguntaModel)
                carregarPerguntas()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao criar pergunta: ${e.message}"
            }
        }
    }

    fun criarPerguntaVerdadeiroFalso(
        idUsuario: Int,
        idGrupo: Int,
        pergunta: String,
        privada: Boolean,
        resposta: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respostaModel = RespostaApiModel(
                    alternativas = emptyList(),
                    texto = null,
                    numero = null,
                    booleano = resposta
                )

                val perguntaModel = PerguntaApiModel(
                    idTipo = 3,
                    idUsuario = idUsuario,
                    idGrupo = idGrupo,
                    pergunta = pergunta,
                    privada = privada,
                    resposta = respostaModel
                )

                repo.criarPergunta(perguntaModel)
                carregarPerguntas()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao criar pergunta: ${e.message}"
            }
        }
    }

    fun criarPerguntaTextoAberto(
        idUsuario: Int,
        idGrupo: Int,
        pergunta: String,
        privada: Boolean,
        resposta: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respostaModel = RespostaApiModel(
                    alternativas = emptyList(),
                    texto = resposta,
                    numero = null,
                    booleano = null
                )

                val perguntaModel = PerguntaApiModel(
                    idTipo = 4,
                    idUsuario = idUsuario,
                    idGrupo = idGrupo,
                    pergunta = pergunta,
                    privada = privada,
                    resposta = respostaModel
                )

                repo.criarPergunta(perguntaModel)
                carregarPerguntas()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Erro ao criar pergunta: ${e.message}"
            }
        }
    }

    fun carregarPerguntasPorUsuarioEGrupo(idUsuario: Int, idGrupo: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = repo.buscarPerguntasPorUsuarioEGrupo(idUsuario, idGrupo)
                _perguntas.value = response.data
            } catch (e: Exception) {
                _error.value = "Erro ao carregar perguntas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletarPergunta(
        perguntaId: Int,
        usuarioId: Int,
        groupId: Int,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repo.deletarPergunta(PerguntaDeleteDTO(perguntaId, usuarioId))
                carregarPerguntasPorUsuarioEGrupo(usuarioId, groupId)
                onSuccess()
            } catch (e: Exception) {
                onError()
            }
        }
    }
}