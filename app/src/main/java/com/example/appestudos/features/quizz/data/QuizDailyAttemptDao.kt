package com.example.appestudos.features.quizz.data

import androidx.room.*

@Dao
interface QuizDailyAttemptDao {
    @Query("SELECT * FROM quiz_daily_attempts ORDER BY date DESC")
    suspend fun getAllAttempts(): List<QuizDailyAttempt>

    @Query("SELECT * FROM quiz_daily_attempts WHERE date = :date")
    suspend fun getAttemptByDate(date: Long): QuizDailyAttempt?

    @Query("SELECT * FROM quiz_daily_attempts WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllAttemptsByUser(userId: Int): List<QuizDailyAttempt>

    @Query("SELECT * FROM quiz_daily_attempts WHERE userId = :userId AND date = :date")
    suspend fun getAttemptByDateAndUser(userId: Int, date: Long): QuizDailyAttempt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizDailyAttempt)

    @Query("SELECT COUNT(*) FROM quiz_daily_attempts WHERE date >= :startDate")
    suspend fun getStreakCount(startDate: Long): Int

    @Query("SELECT COUNT(*) FROM quiz_daily_attempts WHERE userId = :userId AND date >= :startDate")
    suspend fun getStreakCount(userId: Int, startDate: Long): Int

    @Query("SELECT * FROM quiz_daily_attempts WHERE date >= :startDate ORDER BY date DESC")
    suspend fun getRecentAttempts(startDate: Long): List<QuizDailyAttempt>

    @Query("SELECT * FROM quiz_daily_attempts WHERE userId = :userId AND date >= :startDate ORDER BY date DESC")
    suspend fun getRecentAttemptsByUser(userId: Int, startDate: Long): List<QuizDailyAttempt>
} 