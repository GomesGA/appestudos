package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.flashcards.model.FlashcardGroup
import com.example.appestudos.features.flashcards.viewmodel.FlashcardEntity
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel

@Composable
fun CreateFlashcardScreen(
    navController: NavController
) {
    // Obtém a instância do ViewModel
    val viewModel: FlashcardViewModel = viewModel()

    var selectedGroup by remember { mutableStateOf<FlashcardGroup?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") } // novo campo de texto de estudo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Flashcard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Selecione o Grupo:",
                style = MaterialTheme.typography.subtitle1
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(FlashcardGroup.values()) { group ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(60.dp)
                            .background(
                                color = if (selectedGroup == group) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedGroup = group },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = group.icon,
                            contentDescription = group.title,
                            tint = if (selectedGroup == group) Color.White else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título do Flashcard") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Conteúdo de Estudo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Persiste o flashcard (lembre-se de atualizar FlashcardEntity e persistência para armazenar `content`)
                    viewModel.insert(
                        FlashcardEntity(
                            groupId = selectedGroup!!.id,
                            groupTitle = selectedGroup!!.title,
                            iconName = selectedGroup!!.name,
                            title = title,
                            // assumindo que FlashcardEntity foi extendido com `val content: String`
                            content = content
                        )
                    )
                    navController.popBackStack()
                },
                enabled = (selectedGroup != null && title.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Adicionar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateFlashcardScreenPreview() {
    CreateFlashcardScreen(navController = rememberNavController())
}
