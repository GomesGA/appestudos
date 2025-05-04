package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardGroupScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    isPrivateParam: String,
    viewModel: FlashcardViewModel = viewModel()
) {
    var showDeleteConfirmation by remember { mutableStateOf<Int?>(null) }
    val userId = UserManager.getCurrentUser()?.id ?: 0

    // Carrega as perguntas ao entrar na tela
    LaunchedEffect(Unit) { viewModel.carregarPerguntas() }
    val perguntas by viewModel.perguntas.collectAsState()
    val isPrivate = isPrivateParam == "private"
    
    // Filtrando perguntas pelo grupo e status privado/público
    val filteredPerguntas = perguntas.filter { pergunta -> 
        pergunta.idGrupo == groupId && pergunta.gabaritoBooleano == isPrivate
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = groupId }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir grupo",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            items(filteredPerguntas) { pergunta ->
                PerguntaListItem(navController, pergunta)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerguntaListItem(
    navController: NavController,
    pergunta: PerguntaResponseApiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    val titleEncoded = URLEncoder.encode(pergunta.gabaritoTexto ?: "", "UTF-8")
                    val contentEncoded = URLEncoder.encode(pergunta.gabaritoTexto ?: "", "UTF-8")
                    navController.navigate("flashcardDetail/$titleEncoded/$contentEncoded")
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pergunta.gabaritoTexto ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
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
    val decodedTitle = URLDecoder.decode(title, "UTF-8")
    val decodedContent = URLDecoder.decode(content, "UTF-8")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = decodedContent,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
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