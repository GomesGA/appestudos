package com.example.appestudos.features.profile.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.search.data.QuizDatabase
import com.example.appestudos.features.search.data.QuizDailyAttempt
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PerformanceScreen(navController: NavController) {
    val isDark = !MaterialTheme.colors.isLight
    val currentUser = UserManager.getCurrentUser()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current
    val quizDb = remember { QuizDatabase.getInstance(context) }
    val dailyDao = quizDb.quizDailyAttemptDao()
    var completedDays by remember { mutableStateOf<Set<String>>(emptySet()) }
    var currentStreak by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Novo: estado para dia selecionado e detalhes
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }
    var dayDetails by remember { mutableStateOf<QuizDailyAttempt?>(null) }
    var attemptsMap by remember { mutableStateOf<Map<String, QuizDailyAttempt>>(emptyMap()) }

    // Carrega dias completados, streak e map do banco ao mudar o mês
    LaunchedEffect(selectedMonth) {
        scope.launch {
            val start = selectedMonth.atDay(1)
            val end = selectedMonth.atEndOfMonth()
            val calendar = Calendar.getInstance()
            calendar.set(start.year, start.monthValue - 1, start.dayOfMonth, 0, 0, 0)
            val startMillis = calendar.timeInMillis
            calendar.set(end.year, end.monthValue - 1, end.dayOfMonth, 23, 59, 59)
            val endMillis = calendar.timeInMillis

            // Busca todos os dias do mês
            val attempts = dailyDao.getRecentAttempts(startMillis)
            attemptsMap = attempts
                .filter { it.date in startMillis..endMillis }
                .associateBy {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = it.date
                    "%04d-%02d-%02d".format(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                }
            completedDays = attemptsMap.filter { it.value.attempts > 0 }.keys.toSet()
            // Calcula streak real
            currentStreak = calculateCurrentStreakFromAttempts(attempts)
        }
    }

    // Novo: busca detalhes do dia selecionado
    LaunchedEffect(selectedDay) {
        selectedDay?.let { date ->
            val cal = Calendar.getInstance()
            cal.set(date.year, date.monthValue - 1, date.dayOfMonth, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            dayDetails = dailyDao.getAttemptByDate(cal.timeInMillis)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Desempenho") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                backgroundColor = if (isDark) Color.Black else Color.White,
                contentColor = if (isDark) Color.White else Color.Black,
                elevation = 4.dp
            )
        },
        backgroundColor = if (isDark) Color.Black else Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User name
            Text(
                text = "Olá, ${currentUser?.nome ?: "Erro"}",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = if (isDark) Color.White else Color.Black
            )

            Text(
                text = "Sequência atual: $currentStreak dias",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp),
                color = if (isDark) Color.White else Color.Black
            )
            
            // Month selector with custom DropdownMenu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }} ${selectedMonth.year}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Selecionar mês",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(240.dp)
                        .heightIn(max = 320.dp),
                    offset = DpOffset(x = (-60).dp, y = 0.dp)
                ) {
                    val months = (1..12).map { month -> YearMonth.of(selectedMonth.year, month) }
                    months.forEach { month ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (month == selectedMonth)
                                        if (isDark) Color(0xFF01380b) else Color(0xFF339158)
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedMonth = month
                                    expanded = false
                                }
                                .padding(vertical = 22.dp, horizontal = 28.dp)
                        ) {
                            Text(
                                text = month.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() },
                                color = if (month == selectedMonth) Color.White else if (isDark) Color.White else Color.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            
            // Calendar (agora recebe attemptsMap)
            CalendarView(
                yearMonth = selectedMonth,
                attemptsMap = attemptsMap,
                isDark = isDark,
                onDayClick = { selectedDay = it }
            )
        }
    }

    // Dialog de detalhes do dia
    if (selectedDay != null) {
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text("Detalhes do dia ${selectedDay.toString()}") },
            text = {
                if (dayDetails != null) {
                    Column {
                        Text("Perguntas feitas: ${dayDetails!!.attempts}")
                        Text("Acertos: ${dayDetails!!.correctAnswers}")
                        Text("Erros: ${dayDetails!!.attempts - dayDetails!!.correctAnswers}")
                    }
                } else {
                    Text("Nenhuma atividade registrada neste dia.")
                }
            },
            confirmButton = {
                Button(onClick = { selectedDay = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateCurrentStreakFromAttempts(attempts: List<QuizDailyAttempt>): Int {
    if (attempts.isEmpty()) return 0
    val today = Calendar.getInstance()
    var streak = 0
    var currentDate = today.clone() as Calendar
    while (true) {
        val found = attempts.any { attempt ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = attempt.date
            cal.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
            cal.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        }
        if (!found) break
        streak++
        currentDate.add(Calendar.DAY_OF_MONTH, -1)
    }
    return streak
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    attemptsMap: Map<String, QuizDailyAttempt>,
    isDark: Boolean,
    onDayClick: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    
    Column {
        // Week days header
        Row {
            listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (isDark) Color.White else Color.Black
                )
            }
        }
        
        // Calendar grid
        var dayCount = 1
        repeat(6) { week ->
            Row {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < firstDayOfWeek - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else if (dayCount <= daysInMonth) {
                        val currentDate = yearMonth.atDay(dayCount)
                        val key = "%04d-%02d-%02d".format(
                            currentDate.year,
                            currentDate.monthValue,
                            currentDate.dayOfMonth
                        )
                        val attempt = attemptsMap[key]
                        val bgColor = when {
                            attempt == null || attempt.attempts == 0 -> if (isDark) Color(0xFF424242) else Color(0xFFF5F5F5)
                            attempt.correctAnswers >= (attempt.attempts - attempt.correctAnswers) -> if (isDark) Color(0xFF339158) else Color(0xFF4CAF50)
                            else -> Color(0xFFE57373)
                        }
                        
                        Button(
                            onClick = { onDayClick(currentDate) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = bgColor
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = dayCount.toString(),
                                color = if (attempt != null && attempt.attempts > 0)
                                    Color.White
                                else if (isDark) Color.White else Color.Black
                            )
                        }
                        dayCount++
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
} 