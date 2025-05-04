package com.example.appestudos.features.search.data

import androidx.room.*

@Dao
interface QuizAttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: QuizAttempt)

    @Query("SELECT * FROM quiz_attempts WHERE perguntaId = :perguntaId AND localFavoritoId = :localId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getUltimaTentativa(perguntaId: Int, localId: Int): QuizAttempt?

    @Query("SELECT * FROM quiz_attempts WHERE localFavoritoId = :localId")
    suspend fun getTentativasPorLocal(localId: Int): List<QuizAttempt>
} 