package com.example.appestudos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlusOne
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

data class IconOption(
    val icon: ImageVector,
    val id: Int
)

@Composable
fun CreateGroupScreen(navController: NavController) {
    var groupName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<ImageVector?>(null) }

    val iconOptions = listOf(
        IconOption(Icons.Filled.Code, 1),
        IconOption(Icons.Filled.Lan, 2),
        IconOption(Icons.Filled.Computer, 3),
        IconOption(Icons.Filled.AttachMoney, 4),
        IconOption(Icons.Filled.PlusOne, 5),
        IconOption(Icons.Filled.Book, 6),
        IconOption(Icons.Filled.Biotech, 7),
        IconOption(Icons.Filled.AccountBalance, 8)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Grupo de Flashcards") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
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
                            isSelected = selectedIcon == iconOption.icon,
                            onClick = { selectedIcon = iconOption.icon }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botão de Criar
                Button(
                    onClick = {
                        // TODO: Implementar criação do grupo
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 16.dp),
                    enabled = groupName.isNotBlank() && selectedIcon != null
                ) {
                    Text("Criar Grupo")
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