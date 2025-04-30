package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.flashcards.viewmodel.FlashcardEntity
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.features.flashcards.model.FlashcardGroup
import java.net.URLEncoder
import java.net.URLDecoder

@Composable
fun FlashcardGroupScreen(
    navController: NavController,
    groupId: Int,
    groupName: String,
    viewModel: FlashcardViewModel = viewModel()
) {
    // Carrega os flashcards ao entrar na tela
    LaunchedEffect(groupId) { viewModel.load(groupId) }
    val cards by viewModel.cards.collectAsState()

    Scaffold(
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
                contentColor = MaterialTheme.colors.onPrimary
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(cards) { card ->
                FlashcardListItem(navController, card, viewModel)
            }
        }
    }
}

@Composable
fun FlashcardListItem(
    navController: NavController,
    card: FlashcardEntity,
    viewModel: FlashcardViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val icon: ImageVector = FlashcardGroup.valueOf(card.iconName).icon

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja realmente excluir o flashcard '${card.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(card.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                    val titleEncoded = URLEncoder.encode(card.title, "UTF-8")
                    val contentEncoded = URLEncoder.encode(card.content, "UTF-8")
                    navController.navigate("flashcardDetail/$titleEncoded/$contentEncoded")
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSecondary
                )
            }
            
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir flashcard",
                    tint = MaterialTheme.colors.onSecondary
                )
            }
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
        groupName = "Programação"
    )
}