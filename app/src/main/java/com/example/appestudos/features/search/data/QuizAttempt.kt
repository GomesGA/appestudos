package com.example.appestudos.features.search.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val perguntaId: Int,
    val localFavoritoId: Int,
    val timestamp: Long,
    val acertou: Boolean
) 