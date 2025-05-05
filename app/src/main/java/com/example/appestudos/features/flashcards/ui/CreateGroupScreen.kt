package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

data class IconOption(
    val icon: ImageVector,
    val id: Int,
    val path: String
)

@Composable
fun CreateGroupScreen(navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<IconOption?>(null) }
    val viewModel: FlashcardViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val iconOptions = listOf(
        IconOption(Icons.Filled.Code, 1, "androidx.compose.material.icons.filled.Code"),
        IconOption(Icons.Filled.Calculate, 2, "androidx.compose.material.icons.filled.Calculate"),
        IconOption(Icons.Filled.Science, 3, "androidx.compose.material.icons.filled.Science"),
        IconOption(Icons.Filled.Book, 4, "androidx.compose.material.icons.filled.Book"),
        IconOption(Icons.Filled.Public, 5, "androidx.compose.material.icons.filled.Public"),
        IconOption(Icons.Filled.Language, 6, "androidx.compose.material.icons.filled.Language"),
        IconOption(Icons.Filled.Computer, 7, "androidx.compose.material.icons.filled.Computer"),
        IconOption(Icons.Filled.Business, 8, "androidx.compose.material.icons.filled.Business")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Grupo de Flashcards", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                backgroundColor = Color.Black,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nome do Grupo
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nome do Grupo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true
                )

                // Seleção de Ícone
                Text(
                    text = "Escolha um ícone",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Grid de ícones
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(iconOptions) { iconOption ->
                        IconSelectionItem(
                            icon = iconOption.icon,
                            isSelected = selectedIcon == iconOption,
                            onClick = { selectedIcon = iconOption }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botão de Criar
                Button(
                    onClick = {
                        val userId = UserManager.getCurrentUser()?.id ?: 0
                        viewModel.criarGrupo(
                            descricao = groupName,
                            imagemPath = selectedIcon?.path ?: "",
                            usuarioId = userId,
                            onSuccess = { navController.popBackStack() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 16.dp),
                    enabled = groupName.isNotBlank() && selectedIcon != null && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Criar Grupo")
                    }
                }
            }

            // Exibição de erro
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(errorMessage)
                }
            }
        }
    }
}

@Composable
fun IconSelectionItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                color = if (isSelected) Color.Black else Color.LightGray,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview
@Composable
fun CreateGroupScreenPreview() {
    CreateGroupScreen(navController = rememberNavController())
}