package com.example.appestudos.features.intro.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.text.font.FontWeight
import com.example.appestudos.features.flashcards.model.FlashcardGroup
import java.net.URLEncoder

// Modelo para flashcards privados
// (se ainda desejar exibir uma lista própria)
data class Flashcard(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val isPublic: Boolean
)

@Composable
fun HomeScreen(navController: NavController) {
    val scaffoldState = rememberScaffoldState()
    val navigationBarInsets = WindowInsets.navigationBars.asPaddingValues()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        scaffoldState = scaffoldState,
        bottomBar = { MyButtonBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createFlashcard") },
                contentColor = Color.White,
                backgroundColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Adicionar Flashcard",
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            NameProfile()

            // Título dos grupos
            Text(
                text = "Grupos de Flashcards",
                color = Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // Grid de grupos padrão
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(FlashcardGroup.values()) { group ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(80.dp)
                            .background(Color.Black, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                val encoded = URLEncoder.encode(group.title, "UTF-8")
                                navController.navigate("flashcardGroup/${group.id}/$encoded")
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = group.icon,
                            contentDescription = group.title,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = group.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            // Se desejar manter seção de flashcards privados
            Privados()
            val privateFlashcards = listOf(
                Flashcard(5, "Inglês", Icons.Filled.Code, false),
                Flashcard(6, "Espanhol", Icons.Filled.DesktopWindows, false)
            )
            FlashcardList(
                flashcards = privateFlashcards,
                isPublic = false,
                navController = navController
            )

            Spacer(modifier = Modifier.height(navigationBarInsets.calculateBottomPadding()))
        }
    }
}

@Composable
fun NameProfile() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
                .clickable { }
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
                .clickable { }
        )
    }
}

@Composable
fun FlashcardList(
    flashcards: List<Flashcard>,
    isPublic: Boolean,
    navController: NavController
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        flashcards.forEach { flashcard ->
            FlashcardItem(
                flashcard = flashcard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        if (!isPublic) {
                            Toast.makeText(context, flashcard.title, Toast.LENGTH_SHORT).show()
                        }
                    }
            )
        }
    }
}

@Composable
fun FlashcardItem(
    flashcard: Flashcard,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = flashcard.icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = flashcard.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun Privados() {
    Text(
        "Flashcards Privados",
        color = Color.Black,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
fun MyButtonBar(navController: NavController) {
    val context = LocalContext.current
    val items = listOf(
        BottomMenuItem("Home", Icons.Filled.Home),
        BottomMenuItem("Lupa", Icons.Filled.Search),
        BottomMenuItem("Maps", Icons.Filled.LocationOn)
    )
    var selected by remember { mutableStateOf("Home") }

    BottomAppBar(
        cutoutShape = CircleShape,
        backgroundColor = Color.White,
        elevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                selected = selected == item.label,
                onClick = {
                    selected = item.label
                    if (item.label == "Maps") navController.navigate("map")
                    else Toast.makeText(context, item.label, Toast.LENGTH_SHORT).show()
                },
                icon = {
                    Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(30.dp), tint = Color.Black)
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomMenuItem(val label: String, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
