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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.appestudos.features.flashcards.model.PerguntaApiModel
import com.example.appestudos.features.flashcards.model.RespostaApiModel
import com.example.appestudos.features.flashcards.model.TipoPerguntaApiModel
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.features.auth.data.UserManager

@Composable
fun CreateFlashcardScreen(
    navController: NavController
) {
    // Obtém a instância do ViewModel
    val viewModel: FlashcardViewModel = viewModel()

    var selectedGroup by remember { mutableStateOf<FlashcardGroup?>(null) }
    var title by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf<TipoPerguntaApiModel?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Observa os estados do ViewModel
    val tiposPergunta by viewModel.tiposPergunta.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Carrega os tipos de pergunta ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.carregarTiposPergunta()
    }

    // Mostra erro se houver
    LaunchedEffect(error) {
        error?.let {
            // Aqui você pode mostrar um Snackbar ou Toast com a mensagem de erro
            println("Erro: $it")
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
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
                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(60.dp)
                                    .background(
                                        color = if (selectedGroup == group) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedGroup = group },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = group.icon,
                                    contentDescription = group.title,
                                    tint = if (selectedGroup == group) MaterialTheme.colors.onPrimary else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = group.title,
                                    color = if (selectedGroup == group) MaterialTheme.colors.onPrimary else Color.Gray,
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.padding(top = 4.dp),
                                    maxLines = 1
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

                    // Dropdown de tipos de pergunta
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = tiposPergunta.isNotEmpty()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedTipo?.descricao ?: "Selecione o tipo de pergunta",
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Expandir"
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (tiposPergunta.isEmpty()) {
                                DropdownMenuItem(onClick = { expanded = false }) {
                                    Text("Nenhum tipo disponível")
                                }
                            } else {
                                tiposPergunta.forEach { tipo ->
                                    DropdownMenuItem(onClick = {
                                        selectedTipo = tipo
                                        expanded = false
                                    }) {
                                        Text(tipo.descricao)
                                    }
                                }
                            }
                        }
                    }

                    // Checkbox para privado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it }
                        )
                        Text("Privado")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            val idUsuario = UserManager.getCurrentUser()?.id ?: 0
                            val pergunta = PerguntaApiModel(
                                idTipo = selectedTipo?.id,
                                idUsuario = idUsuario,
                                idGrupo = selectedGroup?.id,
                                pergunta = title,
                                privada = isPrivate,
                                resposta = RespostaApiModel() // Adapte para enviar alternativas, texto, etc
                            )
                            viewModel.criarPergunta(
                                pergunta,
                                onSuccess = { navController.popBackStack() }
                            )
                        },
                        enabled = (selectedGroup != null && title.isNotBlank() && selectedTipo != null && !isLoading),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text("Adicionar", color = MaterialTheme.colors.onPrimary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateFlashcardScreenPreview() {
    CreateFlashcardScreen(navController = rememberNavController())
}
