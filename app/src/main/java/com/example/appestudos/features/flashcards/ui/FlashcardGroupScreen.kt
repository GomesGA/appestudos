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
                FlashcardListItem(navController, card)
            }
        }
    }
}

@Composable
fun FlashcardListItem(
    navController: NavController,
    card: FlashcardEntity
) {
    val icon: ImageVector = FlashcardGroup.valueOf(card.iconName).icon
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val titleEncoded = URLEncoder.encode(card.title, "UTF-8")
                val contentEncoded = URLEncoder.encode(card.content, "UTF-8")
                navController.navigate("flashcardDetail/$titleEncoded/$contentEncoded")
            },
        backgroundColor = MaterialTheme.colors.secondary,
        contentColor = MaterialTheme.colors.onSecondary,
        elevation = 6.dp
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = card.title,
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                text = content,
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