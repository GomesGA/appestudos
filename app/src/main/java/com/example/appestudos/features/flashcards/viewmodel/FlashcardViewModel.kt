package com.example.appestudos.features.flashcards.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.appestudos.features.flashcards.repo.FlashcardRepository

/**
 * Modelo de entidade de flashcard para persistência.
 */
data class FlashcardEntity(
    val id: Int = 0,
    val groupId: Int,
    val groupTitle: String,
    val iconName: String,
    val title: String,
    val content: String
)

/**
 * ViewModel para gerenciar flashcards via SQLite (FlashcardRepository).
 */
class FlashcardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FlashcardRepository(app)
    private val _cards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val cards: StateFlow<List<FlashcardEntity>> = _cards

    /**
     * Carrega todos os flashcards do grupo.
     */
    fun load(groupId: Int) {
        viewModelScope.launch {
            _cards.value = repo.getByGroup(groupId)  // retorna agora com `content`
        }
    }

    /**
     * Insere um flashcard no banco.
     */
    fun insert(f: FlashcardEntity) {
        viewModelScope.launch {
            repo.add(f)
        }
    }

    fun delete(flashcardId: Int) {
        viewModelScope.launch {
            repo.delete(flashcardId)
            // Recarrega a lista após deletar
            _cards.value = _cards.value.filter { it.id != flashcardId }
        }
    }
}