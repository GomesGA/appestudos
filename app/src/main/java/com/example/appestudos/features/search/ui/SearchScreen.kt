package com.example.appestudos.features.search.ui

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

// Tipos de perguntas simulados (mock)
data class TipoPergunta(val id: Int, val descricao: String)
val tiposPergunta = listOf(
    TipoPergunta(1, "Múltipla Escolha"),
    TipoPergunta(2, "Numérico"),
    TipoPergunta(3, "Verdadeiro/Falso")
)

@Composable
fun QuizScreen(navController: NavController) {
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode
    val context = LocalContext.current
    val database = remember { FavoriteLocationDatabase(context) }

    var quizStarted by remember { mutableStateOf(false) }
    var tipoSelecionado by remember { mutableStateOf<TipoPergunta?>(null) }
    var favoriteLocations by remember { mutableStateOf<List<FavoriteLocation>>(emptyList()) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var nearestFavorite by remember { mutableStateOf<FavoriteLocation?>(null) }

    // Carregar locais favoritos
    LaunchedEffect(Unit) {
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
                    onClick = { quizStarted = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar")
                }
            } else if (tipoSelecionado == null) {
                Text(
                    text = "Escolha o tipo de pergunta:",
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                tiposPergunta.forEach { tipo ->
                    Button(
                        onClick = { tipoSelecionado = tipo },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(tipo.descricao)
                    }
                }
            } else {
                // Exemplo de tela de resposta para cada tipo
                when (tipoSelecionado!!.id) {
                    1 -> MultiplaEscolhaExemplo(isDark)
                    2 -> NumericoExemplo(isDark)
                    3 -> VerdadeiroFalsoExemplo(isDark)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { tipoSelecionado = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar para tipos")
                }
            }
        }
    }
    // --- Lógica futura ---
    // Aqui será implementado:
    // - Buscar perguntas do banco de dados
    // - Registrar desempenho e localidade
    // - Controlar repetição por localidade e tempo (3 dias)
    // - Só repetir perguntas em outra localidade ou após 3 dias
}

@Composable
fun MultiplaEscolhaExemplo(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Pergunta de Múltipla Escolha",
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Qual é a capital do Brasil?", color = if (isDark) Color.White else Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Brasília") }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Rio de Janeiro") }
        Button(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("São Paulo") }
    }
}

@Composable
fun NumericoExemplo(isDark: Boolean) {
    var resposta by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Pergunta Numérica",
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Quantos estados tem o Brasil?", color = if (isDark) Color.White else Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = resposta,
            onValueChange = { resposta = it },
            label = { Text("Digite sua resposta") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VerdadeiroFalsoExemplo(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Pergunta Verdadeiro/Falso",
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("O Sol é uma estrela?", color = if (isDark) Color.White else Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { }, modifier = Modifier.weight(1f)) { Text("Verdadeiro") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { }, modifier = Modifier.weight(1f)) { Text("Falso") }
        }
    }
} 