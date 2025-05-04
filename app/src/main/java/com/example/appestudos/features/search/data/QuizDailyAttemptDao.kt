package com.example.appestudos.features.search.data

import androidx.room.*
import java.util.Calendar

@Dao
interface QuizDailyAttemptDao {
    @Query("SELECT * FROM quiz_daily_attempts ORDER BY date DESC")
    suspend fun getAllAttempts(): List<QuizDailyAttempt>

    @Query("SELECT * FROM quiz_daily_attempts WHERE date = :date")
    suspend fun getAttemptByDate(date: Long): QuizDailyAttempt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizDailyAttempt)

    @Query("SELECT COUNT(*) FROM quiz_daily_attempts WHERE date >= :startDate")
    suspend fun getStreakCount(startDate: Long): Int

    @Query("SELECT * FROM quiz_daily_attempts WHERE date >= :startDate ORDER BY date DESC")
    suspend fun getRecentAttempts(startDate: Long): List<QuizDailyAttempt>
} 