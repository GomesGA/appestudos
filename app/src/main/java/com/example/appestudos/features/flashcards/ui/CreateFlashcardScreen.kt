package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appestudos.features.flashcards.model.*
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel

@Composable
fun CreateFlashcardScreen(
    navController: NavController
) {
    val viewModel: FlashcardViewModel = viewModel()
    var selectedGroup by remember { mutableStateOf<FlashcardGroup?>(null) }
    var perguntaText by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf<TipoPerguntaApiModel?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Estados para diferentes tipos de resposta
    var alternativas by remember { mutableStateOf(List(4) { AlternativaApiModel(descricao = "", correta = false) }) }
    var selectedCorrectIndex by remember { mutableStateOf<Int?>(null) }
    var respostaNumericaText by remember { mutableStateOf("") }
    var respostaBooleana by remember { mutableStateOf<Boolean?>(null) }

    // Estados do ViewModel
    val tiposPergunta by viewModel.tiposPergunta.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Carrega tipos de pergunta
    LaunchedEffect(Unit) {
        viewModel.carregarTiposPergunta()
    }

    // Validações
    val isFormValid = remember(perguntaText, selectedTipo, selectedGroup) {
        perguntaText.isNotBlank() && selectedTipo != null && selectedGroup != null &&
        when (selectedTipo?.id) {
            1 -> selectedCorrectIndex != null && alternativas.all { it.descricao.isNotBlank() } // Múltipla escolha
            2 -> respostaNumericaText.isNotBlank() && respostaNumericaText.toDoubleOrNull() != null // Numérica
            3 -> respostaBooleana != null // Verdadeiro/Falso
            else -> false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Novo Flashcard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 4.dp,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Seleção de Grupo
                    Text("Selecione o Grupo:", style = MaterialTheme.typography.subtitle1)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .height(160.dp)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(FlashcardGroup.values()) { group ->
                            GroupItem(
                                group = group,
                                isSelected = selectedGroup == group,
                                onSelect = { selectedGroup = group }
                            )
                        }
                    }

                    // Campo de Pergunta
                    OutlinedTextField(
                        value = perguntaText,
                        onValueChange = { perguntaText = it },
                        label = { Text("Pergunta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Seleção de Tipo
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedTipo?.descricao ?: "Selecione o tipo de pergunta")
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Expandir",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
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

                    // Campos de resposta baseados no tipo selecionado
                    when (selectedTipo?.id) {
                        1 -> { // Múltipla escolha
                            alternativas.forEachIndexed { index, alternativa ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedCorrectIndex == index,
                                        onClick = { selectedCorrectIndex = index }
                                    )
                                    OutlinedTextField(
                                        value = alternativa.descricao,
                                        onValueChange = { newValue ->
                                            alternativas = alternativas.toMutableList().also { list ->
                                                list[index] = alternativa.copy(descricao = newValue)
                                            }
                                        },
                                        label = { Text("Alternativa ${index + 1}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                        2 -> { // Numérico
                            OutlinedTextField(
                                value = respostaNumericaText,
                                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) respostaNumericaText = it },
                                label = { Text("Resposta Numérica") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        3 -> { // Verdadeiro/Falso
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = respostaBooleana == true,
                                        onClick = { respostaBooleana = true }
                                    )
                                    Text("Verdadeiro")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = respostaBooleana == false,
                                        onClick = { respostaBooleana = false }
                                    )
                                    Text("Falso")
                                }
                            }
                        }
                    }

                    // Checkbox Privado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it }
                        )
                        Text("Privado")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botão de Criar
                    Button(
                        onClick = {
                            val idUsuario = UserManager.getCurrentUser()?.id ?: 0
                            val resposta = RespostaApiModel(
                                alternativas = if (selectedTipo?.id == 1) {
                                    alternativas.mapIndexed { index, alt ->
                                        alt.copy(correta = index == selectedCorrectIndex)
                                    }
                                } else null,
                                numero = if (selectedTipo?.id == 2) respostaNumericaText.toIntOrNull() else null,
                                booleano = if (selectedTipo?.id == 3) respostaBooleana else null
                            )
                            
                            val pergunta = PerguntaApiModel(
                                idTipo = selectedTipo?.id,
                                idUsuario = idUsuario,
                                idGrupo = selectedGroup?.id,
                                pergunta = perguntaText,
                                privada = isPrivate,
                                resposta = resposta
                            )
                            
                            viewModel.criarPergunta(
                                pergunta = pergunta,
                                onSuccess = { navController.popBackStack() }
                            )
                        },
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Criar Flashcard")
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
private fun GroupItem(
    group: FlashcardGroup,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .size(60.dp)
            .background(
                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = group.icon,
            contentDescription = group.title,
            tint = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = group.title,
            color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
