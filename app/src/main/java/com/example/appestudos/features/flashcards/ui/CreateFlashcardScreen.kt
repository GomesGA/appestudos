package com.example.appestudos.features.flashcards.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appestudos.features.flashcards.model.PerguntaTipo
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.features.flashcards.viewmodel.FlashcardViewModel
import com.example.appestudos.ui.theme.LocalThemeManager
import androidx.compose.ui.graphics.Color

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
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode

    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val cardColor = if (isDark) Color(0xFF222222) else Color(0xFFF5F5F5)
    val textColor = if (isDark) Color.White else Color.Black
    val buttonColor = if (isDark) Color(0xFF339158) else Color(0xFF4CAF50)
    val buttonTextColor = Color.White

    LaunchedEffect(Unit) {
        viewModel.carregarTiposPergunta()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Flashcard", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .background(backgroundColor)
        ) {
            item {
                OutlinedTextField(
                    value = pergunta,
                    onValueChange = { pergunta = it },
                    label = { Text("Pergunta", color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = buttonColor,
                        unfocusedBorderColor = cardColor,
                        cursorColor = buttonColor,
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Tipo de Flashcard", style = MaterialTheme.typography.titleMedium, color = textColor)
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
                            buttonColor
                        else
                            cardColor
                    )
                ) {
                    Text(
                        text = tipo.descricao,
                        modifier = Modifier.padding(16.dp),
                        color = if (selectedType?.id == tipo.id) buttonTextColor else textColor
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
                                label = { Text("Alternativa ${index + 1}", color = textColor) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = buttonColor,
                                    unfocusedBorderColor = cardColor,
                                    cursorColor = buttonColor,
                                    focusedLabelColor = textColor,
                                    unfocusedLabelColor = textColor,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = alternativasCorretas[index],
                                    onCheckedChange = { checked ->
                                        alternativasCorretas = alternativasCorretas.toMutableList().apply {
                                            set(index, checked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = buttonColor,
                                        uncheckedColor = cardColor
                                    )
                                )
                                Text("Correta", color = textColor)
                            }
                        }
                    }
                }
                2 -> { // Numérico
                    item {
                        OutlinedTextField(
                            value = respostaNumerica,
                            onValueChange = { respostaNumerica = it },
                            label = { Text("Resposta Numérica", color = textColor) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = buttonColor,
                                unfocusedBorderColor = cardColor,
                                cursorColor = buttonColor,
                                focusedLabelColor = textColor,
                                unfocusedLabelColor = textColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            )
                        )
                    }
                }
                3 -> { // Verdadeiro/Falso
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val selectedColor = buttonColor
                            val unselectedColor = cardColor
                            val textSelected = buttonTextColor
                            val textUnselected = textColor
                            Button(
                                onClick = { respostaBooleana = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (respostaBooleana == true) selectedColor else unselectedColor
                                )
                            ) {
                                Text("Verdadeiro", color = if (respostaBooleana == true) textSelected else textUnselected)
                            }
                            Button(
                                onClick = { respostaBooleana = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (respostaBooleana == false) selectedColor else unselectedColor
                                )
                            ) {
                                Text("Falso", color = if (respostaBooleana == false) textSelected else textUnselected)
                            }
                        }
                    }
                }
                4 -> { // Texto Aberto
                    item {
                        OutlinedTextField(
                            value = respostaTexto,
                            onValueChange = { respostaTexto = it },
                            label = { Text("Texto", color = textColor) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = buttonColor,
                                unfocusedBorderColor = cardColor,
                                cursorColor = buttonColor,
                                focusedLabelColor = textColor,
                                unfocusedLabelColor = textColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = pergunta.isNotBlank() && selectedType != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF00C853) else buttonColor,
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = MaterialTheme.shapes.medium,
                    border = if (isDark) BorderStroke(
                        width = 2.dp,
                        color = Color(0xFF00C853).copy(alpha = 0.2f)
                    ) else null
                ){
                    Text(
                        "Criar Flashcard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
        }
    }
}