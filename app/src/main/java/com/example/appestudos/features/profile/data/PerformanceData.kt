package com.example.appestudos.features.profile.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.appestudos.features.auth.data.UserManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class UserPerformance(
    val userId: Int,
    val completedDays: Set<String> = emptySet(),
    val currentStreak: Int = 0
)

object PerformanceManager {
    private lateinit var prefs: SharedPreferences
    private const val PREF_NAME = "performance_prefs"
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @RequiresApi(Build.VERSION_CODES.O)
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Adiciona alguns dias de exemplo para demonstração
        val exampleDays = setOf(
            LocalDate.now().minusDays(1).format(formatter),
            LocalDate.now().minusDays(2).format(formatter),
            LocalDate.now().minusDays(3).format(formatter),
            LocalDate.now().minusDays(5).format(formatter),
            LocalDate.now().minusDays(6).format(formatter),
            LocalDate.now().minusDays(7).format(formatter)
        )
        
        // Salva os dias de exemplo para o usuário atual
        val currentUser = UserManager.getCurrentUser()
        if (currentUser != null) {
            val performance = getPerformance(currentUser.id)
            if (performance.completedDays.isEmpty()) {
                val updatedPerformance = performance.copy(
                    completedDays = exampleDays,
                    currentStreak = 3 // Streak atual é 3 dias
                )
                savePerformance(updatedPerformance)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markDayAsCompleted(userId: Int) {
        val currentDate = LocalDate.now().format(formatter)
        val performance = getPerformance(userId)
        val updatedDays = performance.completedDays + currentDate
        val updatedStreak = calculateStreak(updatedDays)
        
        val updatedPerformance = performance.copy(
            completedDays = updatedDays,
            currentStreak = updatedStreak
        )
        
        savePerformance(updatedPerformance)
    }

    fun getPerformance(userId: Int): UserPerformance {
        val json = prefs.getString("performance_$userId", null)
        return if (json != null) {
            Json.decodeFromString<UserPerformance>(json)
        } else {
            UserPerformance(userId)
        }
    }

    private fun savePerformance(performance: UserPerformance) {
        val json = Json.encodeToString(performance)
        prefs.edit().putString("performance_${performance.userId}", json).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateStreak(days: Set<String>): Int {
        if (days.isEmpty()) return 0
        
        val sortedDates = days.map { LocalDate.parse(it, formatter) }
            .sortedDescending()
        
        var streak = 0
        var currentDate = LocalDate.now()
        
        for (date in sortedDates) {
            if (date == currentDate || date == currentDate.minusDays(1)) {
                streak++
                currentDate = date
            } else {
                break
            }
        }
        
        return streak
    }
} 