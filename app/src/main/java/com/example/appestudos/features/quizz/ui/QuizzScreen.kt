package com.example.appestudos.features.quizz.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appestudos.features.map.data.FavoriteLocationDatabase
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.map.ui.FavoriteLocation
import com.example.appestudos.ui.theme.LocalThemeManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.features.flashcards.model.PerguntaResponseApiModel
import com.example.appestudos.features.flashcards.model.AlternativaApiModel
import com.example.appestudos.features.quizz.data.QuizDatabase
import com.example.appestudos.features.quizz.data.QuizAttempt
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.appestudos.features.quizz.data.QuizDailyAttempt
import java.util.Calendar


@Composable
fun QuizScreen(navController: NavController) {
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode
    val context = LocalContext.current
    val database = remember { FavoriteLocationDatabase(context) }
    val quizDb = remember { QuizDatabase.getInstance(context) }
    val quizDao = quizDb.quizAttemptDao()
    val dailyDao = quizDb.quizDailyAttemptDao()
    val viewModel: FlashcardViewModel = viewModel()
    val userId = UserManager.getCurrentUser()?.id ?: 0
    val perguntas by viewModel.perguntas.collectAsState()
    var quizStarted by remember { mutableStateOf(false) }
    var perguntaAtual by remember { mutableStateOf<PerguntaResponseApiModel?>(null) }
    var respostaSelecionada by remember { mutableStateOf<AlternativaApiModel?>(null) }
    var resultado by remember { mutableStateOf<String?>(null) }
    var respostaNumerica by remember { mutableStateOf("") }
    var respostaVF by remember { mutableStateOf<Boolean?>(null) }
    var favoriteLocations by remember { mutableStateOf<List<FavoriteLocation>>(emptyList()) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var nearestFavorite by remember { mutableStateOf<FavoriteLocation?>(null) }
    var dailyAttempts by remember { mutableStateOf<List<QuizDailyAttempt>>(emptyList()) }
    var currentStreak by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Carregar perguntas reais ao entrar
    LaunchedEffect(Unit) {
        viewModel.carregarPerguntas() // Busca todas (públicas e privadas)
        UserManager.getCurrentUserId()?.let { userId ->
            favoriteLocations = database.getAllFavoriteLocations(userId)
        }
    }
    // Obter localização atual
    LaunchedEffect(Unit) {
        try {
            val permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permission == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            } else {
                // Permissão não concedida
                // Você pode mostrar uma mensagem ou lidar de outra forma
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Lide com a exceção de permissão aqui
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // Calcular local favorito mais próximo
    LaunchedEffect(currentLocation, favoriteLocations) {
        currentLocation?.let { current ->
            var minDistance = Float.MAX_VALUE
            var nearest: FavoriteLocation? = null
            favoriteLocations.forEach { favorite ->
                val results = FloatArray(1)
                android.location.Location.distanceBetween(
                    current.latitude,
                    current.longitude,
                    favorite.location.latitude,
                    favorite.location.longitude,
                    results
                )
                val distance = results[0]
                if (distance < minDistance) {
                    minDistance = distance
                    nearest = favorite
                }
            }
            nearestFavorite = nearest
        }
    }

    // Carregar tentativas diárias ao entrar na tela
    LaunchedEffect(Unit) {
        val today = QuizDailyAttempt.getTodayTimestamp()
        val thirtyDaysAgo = today - (30L * 24 * 60 * 60 * 1000)
        dailyAttempts = dailyDao.getRecentAttempts(thirtyDaysAgo)
        currentStreak = calculateCurrentStreak(dailyAttempts)
    }

    // Atualizar tentativa diária quando uma pergunta é respondida
    LaunchedEffect(resultado) {
        resultado?.let {
            val today = QuizDailyAttempt.getTodayTimestamp()
            val existingAttempt = dailyDao.getAttemptByDateAndUser(userId, today)
            val newAttempt = existingAttempt?.let { attempt ->
                attempt.copy(
                    attempts = attempt.attempts + 1,
                    correctAnswers = attempt.correctAnswers + (if (it.contains("acertou")) 1 else 0),
                    userId = userId
                )
            } ?: QuizDailyAttempt(
                date = today,
                attempts = 1,
                correctAnswers = if (it.contains("acertou")) 1 else 0,
                userId = userId
            )
            dailyDao.insertAttempt(newAttempt)
            val thirtyDaysAgo = today - (30L * 24 * 60 * 60 * 1000)
            dailyAttempts = dailyDao.getRecentAttemptsByUser(userId, thirtyDaysAgo)
            currentStreak = calculateCurrentStreak(dailyAttempts)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF121212) else Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Topo: botão de voltar e título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = if (isDark) Color.White else Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quiz",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            }
            // Box/Card com local favorito ou mensagem
            if (favoriteLocations.isEmpty()) {
                Card(
                    backgroundColor = if (isDark) Color(0xFF222222) else Color(0xFFF5F5F5),
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Você precisa criar um local favorito para usar o quiz.",
                            color = if (isDark) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                return@Column
            } else {
                Card(
                    backgroundColor = if (isDark) Color(0xFF222222) else Color(0xFFF5F5F5),
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Local favorito mais próximo: ${nearestFavorite?.name ?: "Carregando..."}",
                            color = if (isDark) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            if (!quizStarted) {
                Button(
                    onClick = {
                        scope.launch {
                            val localId = nearestFavorite?.id ?: return@launch
                            val agora = System.currentTimeMillis()
                            val tresDiasMs = 3 * 24 * 60 * 60 * 1000L
                            val perguntasValidas = perguntas.filter { pergunta ->
                                val ultimaTentativa = quizDao.getUltimaTentativa(pergunta.id, localId)
                                val podeRepetir = ultimaTentativa == null || agora - ultimaTentativa.timestamp > tresDiasMs
                                podeRepetir && (
                                    (pergunta.alternativas?.isNotEmpty() == true) ||
                                    (pergunta.gabaritoNumero != null) ||
                                    (pergunta.gabaritoBooleano != null)
                                )
                            }
                            perguntaAtual = if (perguntasValidas.isNotEmpty()) perguntasValidas.random() else null
                            respostaSelecionada = null
                            resultado = null
                            respostaNumerica = ""
                            respostaVF = null
                            quizStarted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar")
                }
            } else if (perguntaAtual == null) {
                val tresDiasMs = 3 * 24 * 60 * 60 * 1000L
                val localId = nearestFavorite?.id ?: 0
                var tempoRestante by remember { mutableStateOf<Long?>(null) }

                // Efeito para atualizar o tempo restante em tempo real
                LaunchedEffect(perguntas, nearestFavorite) {
                    while (true) {
                        val tentativasRecentes = quizDao.getTentativasPorLocal(localId)
                        val agora = System.currentTimeMillis()
                        val temposRestantes = tentativasRecentes.map { tentativa ->
                            val tempoRestante = (tentativa.timestamp + tresDiasMs) - agora
                            if (tempoRestante > 0) tempoRestante else null
                        }.filterNotNull()
                        tempoRestante = temposRestantes.minOrNull()
                        delay(1000) // Atualiza a cada segundo
                    }
                }

                Text(
                    "Nenhuma pergunta disponível para o quiz.",
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
                tempoRestante?.let { tempo ->
                    val horas = tempo / (1000 * 60 * 60)
                    val minutos = (tempo / (1000 * 60)) % 60
                    val segundos = (tempo / 1000) % 60
                    Text(
                        text = "Próximo quiz disponível em: %02d:%02d:%02d".format(horas, minutos, segundos),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(onClick = { quizStarted = false }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Voltar")
                }
            } else {
                // Exibe a pergunta e as opções
                Text(
                    text = perguntaAtual?.descricao ?: "Sem título",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                val localId = nearestFavorite?.id ?: 0
                // Múltipla escolha
                if (perguntaAtual?.alternativas?.isNotEmpty() == true) {
                    perguntaAtual!!.alternativas!!.forEach { alternativa ->
                        Button(
                            onClick = {
                                respostaSelecionada = alternativa
                                resultado = if (alternativa.correta) "Você acertou!" else "Você errou!"
                                scope.launch {
                                    quizDao.insertAttempt(
                                        QuizAttempt(
                                            perguntaId = perguntaAtual!!.id,
                                            localFavoritoId = localId,
                                            timestamp = System.currentTimeMillis(),
                                            acertou = alternativa.correta
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            enabled = resultado == null
                        ) {
                            Text(alternativa.descricao)
                        }
                    }
                }
                // Numérico
                else if (perguntaAtual?.gabaritoNumero != null) {
                    OutlinedTextField(
                        value = respostaNumerica,
                        onValueChange = { respostaNumerica = it },
                        label = { Text("Digite sua resposta numérica", color = if (isDark) Color(0xFF43FF64) else Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = resultado == null,
                        textStyle = LocalTextStyle.current.copy(color = if (isDark) Color.White else Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = if (isDark) Color(0xFF43FF64) else MaterialTheme.colors.primary,
                            unfocusedBorderColor = if (isDark) Color(0xFF43FF64) else Color.Gray,
                            cursorColor = if (isDark) Color(0xFF43FF64) else MaterialTheme.colors.primary
                        )
                    )
                    Button(
                        onClick = {
                            val acertou = respostaNumerica.toIntOrNull() == perguntaAtual?.gabaritoNumero
                            resultado = if (acertou) "Você acertou!" else "Você errou!"
                            scope.launch {
                                quizDao.insertAttempt(
                                    QuizAttempt(
                                        perguntaId = perguntaAtual!!.id,
                                        localFavoritoId = localId,
                                        timestamp = System.currentTimeMillis(),
                                        acertou = acertou
                                    )
                                )
                            }
                        },
                        enabled = resultado == null && respostaNumerica.isNotBlank(),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Responder")
                    }
                }
                // Verdadeiro/Falso
                else if (perguntaAtual?.gabaritoBooleano != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                respostaVF = true
                                val acertou = perguntaAtual?.gabaritoBooleano == true
                                resultado = if (acertou) "Você acertou!" else "Você errou!"
                                scope.launch {
                                    quizDao.insertAttempt(
                                        QuizAttempt(
                                            perguntaId = perguntaAtual!!.id,
                                            localFavoritoId = localId,
                                            timestamp = System.currentTimeMillis(),
                                            acertou = acertou
                                        )
                                    )
                                }
                            },
                            enabled = resultado == null
                        ) { Text("Verdadeiro") }
                        Button(
                            onClick = {
                                respostaVF = false
                                val acertou = perguntaAtual?.gabaritoBooleano == false
                                resultado = if (acertou) "Você acertou!" else "Você errou!"
                                scope.launch {
                                    quizDao.insertAttempt(
                                        QuizAttempt(
                                            perguntaId = perguntaAtual!!.id,
                                            localFavoritoId = localId,
                                            timestamp = System.currentTimeMillis(),
                                            acertou = acertou
                                        )
                                    )
                                }
                            },
                            enabled = resultado == null
                        ) { Text("Falso") }
                    }
                }
                // Resultado
                resultado?.let {
                    val isAcerto = it.contains("acertou")
                    Text(
                        text = it,
                        color = if (isAcerto) {
                            if (isDark) Color(0xFF43FF64) else Color(0xFF4CAF50)
                        } else {
                            if (isDark) Color(0xFFFF5252) else Color.Red
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val localId = nearestFavorite?.id ?: return@launch
                                val agora = System.currentTimeMillis()
                                val tresDiasMs = 3 * 24 * 60 * 60 * 1000L
                                val perguntasValidas = perguntas.filter { pergunta ->
                                    val ultimaTentativa = quizDao.getUltimaTentativa(pergunta.id, localId)
                                    val podeRepetir = ultimaTentativa == null || agora - ultimaTentativa.timestamp > tresDiasMs
                                    podeRepetir && (
                                        (pergunta.alternativas?.isNotEmpty() == true) ||
                                        (pergunta.gabaritoNumero != null) ||
                                        (pergunta.gabaritoBooleano != null)
                                    )
                                }
                                perguntaAtual = if (perguntasValidas.isNotEmpty()) perguntasValidas.random() else null
                                respostaSelecionada = null
                                resultado = null
                                respostaNumerica = ""
                                respostaVF = null
                            }
                        },
                        modifier = Modifier.padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isDark) Color(0xFF00C853) else Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Próxima Pergunta")
                    }
                    Button(
                        onClick = { quizStarted = false },
                        modifier = Modifier.padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isDark) Color(0xFF222222) else Color(0xFFF5F5F5),
                            contentColor = if (isDark) Color.White else Color.Black
                        )
                    ) {
                        Text("Sair do Quiz")
                    }
                }
            }
        }
    }
}

private fun calculateCurrentStreak(attempts: List<QuizDailyAttempt>): Int {
    if (attempts.isEmpty()) return 0
    
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = QuizDailyAttempt.getTodayTimestamp()
    
    var streak = 0
    var currentDate = calendar.timeInMillis
    
    while (true) {
        val attempt = attempts.find { it.date == currentDate }
        if (attempt == null) break
        
        streak++
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        currentDate = calendar.timeInMillis
    }
    
    return streak
} 