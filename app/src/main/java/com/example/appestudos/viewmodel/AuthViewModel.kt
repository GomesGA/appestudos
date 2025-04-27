package com.example.appestudos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appestudos.api.ApiService
import com.example.appestudos.api.LoginRequestDTO
import com.example.appestudos.api.UsuarioRequestDTO
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val apiService = ApiService(ApiClient.httpClient)

    /**
     * Tenta fazer login e retorna o resultado via callback.
     * @param email — e-mail digitado
     * @param senha — senha digitada
     * @param onResult — (sucesso, mensagem) -> Unit
     */
    fun login(email: String, senha: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequestDTO(email, senha))
                if (response.success) {
                    onResult(true, response.message ?: "Login realizado com sucesso")
                } else {
                    onResult(false, response.message ?: "Usuário ou senha incorretos")
                }
            } catch (e: Exception) {
                onResult(false, "Erro de rede: ${e.localizedMessage}")
            }
        }
    }

    fun register(
        nome: String,
        email: String,
        senha: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // chama POST /api/usuarios/registrar
                val resp = apiService.registrar(
                    UsuarioRequestDTO(nome = nome, email = email, senha = senha)
                )
                if (resp.success) {
                    onResult(true, resp.message ?: "Cadastro realizado com sucesso")
                } else {
                    onResult(false, resp.message ?: "Falha no cadastro")
                }
            } catch (e: Exception) {
                onResult(false, "Erro de rede: ${e.localizedMessage}")
            }
        }
    }
}
