package com.example.appestudos.features.profile.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.appestudos.features.profile.data.PerformanceManager
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PerformanceScreen(navController: NavController) {
    val isDark = !MaterialTheme.colors.isLight
    val currentUser = UserManager.getCurrentUser()
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val performance = remember { mutableStateOf(PerformanceManager.getPerformance(currentUser?.id ?: 0)) }
    var expanded by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
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
                text = "Sequência atual: ${performance.value.currentStreak} dias",
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
            
            // Calendar
            CalendarView(
                yearMonth = selectedMonth,
                completedDays = performance.value.completedDays,
                isDark = isDark
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    completedDays: Set<String>,
    isDark: Boolean
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
                        val isCompleted = completedDays.contains(currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .background(
                                    if (isCompleted) 
                                        if (isDark) Color(0xFF01380b) else Color(0xFF339158)
                                    else 
                                        if (isDark) Color(0xFF424242) else Color(0xFFF5F5F5)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color.Gray else Color.LightGray,
                                    MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCount.toString(),
                                color = if (isCompleted) 
                                    Color.White
                                else 
                                    if (isDark) Color.White else Color.Black
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