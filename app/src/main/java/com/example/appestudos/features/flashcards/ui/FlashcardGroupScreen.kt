package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.features.flashcards.model.PerguntaResponseApiModel
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

@Composable
fun FlashcardGroupScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    isPrivateParam: String,
    viewModel: FlashcardViewModel = viewModel()
) {
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
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                modifier = Modifier.statusBarsPadding()
            )
        },
        backgroundColor = MaterialTheme.colors.background
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
    }
}

@Composable
fun PerguntaListItem(
    navController: NavController,
    pergunta: PerguntaResponseApiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = MaterialTheme.colors.secondary,
        contentColor = MaterialTheme.colors.onSecondary,
        elevation = 6.dp
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = pergunta.gabaritoTexto ?: "",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSecondary
            )
        }
    }
}

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
                backgroundColor = MaterialTheme.colors.primaryVariant,
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = decodedContent,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardGroupScreenPreview() {
    FlashcardGroupScreen(
        navController = rememberNavController(),
        groupId = 1,
        groupName = "Programação",
        isPrivateParam = "public"
    )
}