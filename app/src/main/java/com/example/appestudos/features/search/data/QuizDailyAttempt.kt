package com.example.appestudos.features.search.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "quiz_daily_attempts")
data class QuizDailyAttempt(
    @PrimaryKey
    val date: Long, // Timestamp do início do dia
    val attempts: Int, // Número de tentativas no dia
    val correctAnswers: Int, // Número de respostas corretas
    val userId: Int // ID do usuário dono da tentativa
) {
    companion object {
        fun getTodayTimestamp(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }
} 