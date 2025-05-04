package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.features.flashcards.model.PerguntaResponseApiModel
import com.example.appestudos.features.flashcards.model.FlashcardGroup
import com.example.appestudos.features.auth.data.UserManager
import java.net.URLEncoder
import java.net.URLDecoder
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.draw.alpha
import com.example.appestudos.ui.theme.LocalThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardGroupScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    isPrivateParam: String,
    viewModel: FlashcardViewModel = viewModel()
) {
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode
    var showDeleteConfirmation by remember { mutableStateOf<Int?>(null) }
    var showDeletePerguntaConfirmation by remember { mutableStateOf<Int?>(null) }
    val userId = UserManager.getCurrentUser()?.id ?: 0

    // Carrega as perguntas ao entrar na tela
    LaunchedEffect(Unit) { viewModel.carregarPerguntasPorUsuarioEGrupo(userId, groupId) }
    val perguntas by viewModel.perguntas.collectAsState()
    val isPrivate = isPrivateParam == "private"
    
    val perguntasFiltradas = perguntas.filter { it.idGrupo == groupId }

    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val cardColor = if (isDark) Color(0xFF222222) else Color(0xFFF5F5F5)
    val textColor = if (isDark) Color.White else Color.Black
    val fabColor = if (isDark) Color(0xFF27391C) else Color(0xFF255F38)
    val fabIconColor = Color.White

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(backgroundColor),
        topBar = {
            TopAppBar(
                title = { Text(groupName, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    if (isPrivate) {
                        IconButton(onClick = { showDeleteConfirmation = groupId }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Excluir grupo",
                                tint = Color.Red
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("createFlashcard/$groupId/$isPrivateParam")
                },
                contentColor = Color.White,
                containerColor = fabColor
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Criar Flashcard",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        if (perguntasFiltradas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum flashcard encontrado.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .navigationBarsPadding()
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                items(perguntasFiltradas) { pergunta ->
                    PerguntaListItemComDelete(navController, pergunta, isPrivate, onDelete = { showDeletePerguntaConfirmation = it }, cardColor = cardColor, textColor = textColor)
                }
            }
        }

        // Delete confirmation dialog
        showDeleteConfirmation?.let { groupId ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Confirmar exclusão") },
                text = { Text("Tem certeza que deseja excluir este grupo?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletarGrupo(
                                grupoId = groupId,
                                usuarioId = userId,
                                onSuccess = {
                                    showDeleteConfirmation = null
                                    navController.popBackStack()
                                },
                                onError = {
                                    showDeleteConfirmation = null
                                }
                            )
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = null }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Dialog de confirmação para deletar flashcard individual
        showDeletePerguntaConfirmation?.let { perguntaId ->
            AlertDialog(
                onDismissRequest = { showDeletePerguntaConfirmation = null },
                title = { Text("Confirmar exclusão") },
                text = { Text("Tem certeza que deseja excluir este flashcard?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deletarPergunta(
                                perguntaId = perguntaId,
                                usuarioId = userId,
                                groupId = groupId,
                                onSuccess = {
                                    showDeletePerguntaConfirmation = null
                                },
                                onError = {
                                    showDeletePerguntaConfirmation = null
                                }
                            )
                        }
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePerguntaConfirmation = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerguntaListItemComDelete(
    navController: NavController,
    pergunta: PerguntaResponseApiModel,
    isPrivate: Boolean,
    onDelete: (Int) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    var mostrarResposta by remember { mutableStateOf(false) }
    val titulo = pergunta.descricao ?: "Sem título"
    val resposta = when {
        !pergunta.gabaritoTexto.isNullOrBlank() -> pergunta.gabaritoTexto!!
        pergunta.gabaritoNumero != null -> pergunta.gabaritoNumero.toString()
        pergunta.gabaritoBooleano != null -> if (pergunta.gabaritoBooleano == true) "Verdadeiro" else "Falso"
        pergunta.alternativas?.isNotEmpty() == true -> {
            pergunta.alternativas.firstOrNull { it.correta }?.descricao ?: ""
        }
        else -> ""
    }
    val isTextoAberto = !pergunta.gabaritoTexto.isNullOrBlank() &&
        pergunta.gabaritoNumero == null && pergunta.gabaritoBooleano == null && (pergunta.alternativas == null || pergunta.alternativas.isEmpty())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (isTextoAberto) {
                    val titleEncoded = URLEncoder.encode(titulo, "UTF-8")
                    val contentEncoded = URLEncoder.encode(resposta, "UTF-8")
                    navController.navigate("flashcardDetail/$titleEncoded/$contentEncoded")
                } else {
                    mostrarResposta = !mostrarResposta
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                if (isPrivate) {
                    IconButton(onClick = { onDelete(pergunta.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir flashcard",
                            tint = Color.Red
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (isTextoAberto) {
                // Não exibe resposta, só redireciona ao clicar
            } else if (mostrarResposta) {
                Text(
                    text = resposta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            } else {
                // Resposta totalmente sensurada
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(textColor.copy(alpha = 0.7f))
                    )
                    Text(
                        text = resposta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Transparent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardDetailScreen(
    navController: NavController,
    title: String,
    content: String
) {
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode
    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val appBarColor = if (isDark) Color(0xFF222222) else Color(0xFF4CAF50)
    val appBarTextColor = Color.White
    val decodedTitle = URLDecoder.decode(title, "UTF-8")
    val decodedContent = URLDecoder.decode(content, "UTF-8")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedTitle, color = appBarTextColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = appBarTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appBarColor,
                    titleContentColor = appBarTextColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .background(backgroundColor)
        ) {
            Text(
                text = decodedContent,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

@Composable
fun FlashcardGroupScreenPreview() {
    FlashcardGroupScreen(
        navController = rememberNavController(),
        groupId = 1,
        groupName = "Programação",
        isPrivateParam = "public"
    )
}