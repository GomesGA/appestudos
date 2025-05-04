package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appestudos.features.flashcards.model.PerguntaTipo
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlashcardScreen(
    navController: NavController,
    groupId: Int,
    isPrivate: Boolean,
    viewModel: FlashcardViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf<PerguntaTipo?>(null) }
    var pergunta by remember { mutableStateOf("") }
    var alternativas by remember { mutableStateOf(List(4) { "" }) }
    var alternativasCorretas by remember { mutableStateOf(List(4) { false }) }
    var respostaNumerica by remember { mutableStateOf("") }
    var respostaBooleana by remember { mutableStateOf<Boolean?>(null) }
    var respostaTexto by remember { mutableStateOf("") }

    val tiposPergunta by viewModel.tiposPergunta.collectAsState()
    val userId = UserManager.getCurrentUser()?.id ?: 0

    LaunchedEffect(Unit) {
        viewModel.carregarTiposPergunta()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Flashcard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = pergunta,
                    onValueChange = { pergunta = it },
                    label = { Text("Pergunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Tipo de Flashcard", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(tiposPergunta) { tipo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedType = tipo },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedType?.id == tipo.id)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = tipo.descricao,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            when (selectedType?.id) {
                1 -> { // Múltipla Escolha
                    items(alternativas.size) { index ->
                        Column {
                            OutlinedTextField(
                                value = alternativas[index],
                                onValueChange = { newValue ->
                                    alternativas = alternativas.toMutableList().apply {
                                        set(index, newValue)
                                    }
                                },
                                label = { Text("Alternativa ${index + 1}") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = alternativasCorretas[index],
                                    onCheckedChange = { checked ->
                                        alternativasCorretas = alternativasCorretas.toMutableList().apply {
                                            set(index, checked)
                                        }
                                    }
                                )
                                Text("Correta")
                            }
                        }
                    }
                }
                2 -> { // Numérico
                    item {
                        OutlinedTextField(
                            value = respostaNumerica,
                            onValueChange = { respostaNumerica = it },
                            label = { Text("Resposta Numérica") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                3 -> { // Verdadeiro/Falso
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { respostaBooleana = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (respostaBooleana == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("Verdadeiro")
                            }
                            Button(
                                onClick = { respostaBooleana = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (respostaBooleana == false)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text("Falso")
                            }
                        }
                    }
                }
                4 -> { // Texto Aberto
                    item {
                        OutlinedTextField(
                            value = respostaTexto,
                            onValueChange = { respostaTexto = it },
                            label = { Text("Texto") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        when (selectedType?.id) {
                            1 -> {
                                viewModel.criarPerguntaMultiplaEscolha(
                                    idUsuario = userId,
                                    idGrupo = groupId,
                                    pergunta = pergunta,
                                    privada = isPrivate,
                                    alternativas = alternativas,
                                    corretas = alternativasCorretas,
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                            2 -> {
                                viewModel.criarPerguntaNumerica(
                                    idUsuario = userId,
                                    idGrupo = groupId,
                                    pergunta = pergunta,
                                    privada = isPrivate,
                                    resposta = respostaNumerica.toIntOrNull() ?: 0,
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                            3 -> {
                                viewModel.criarPerguntaVerdadeiroFalso(
                                    idUsuario = userId,
                                    idGrupo = groupId,
                                    pergunta = pergunta,
                                    privada = isPrivate,
                                    resposta = respostaBooleana ?: false,
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                            4 -> {
                                viewModel.criarPerguntaTextoAberto(
                                    idUsuario = userId,
                                    idGrupo = groupId,
                                    pergunta = pergunta,
                                    privada = isPrivate,
                                    resposta = respostaTexto,
                                    onSuccess = { navController.popBackStack() }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pergunta.isNotBlank() && selectedType != null
                ) {
                    Text("Criar Flashcard")
                }
            }
        }
    }
}