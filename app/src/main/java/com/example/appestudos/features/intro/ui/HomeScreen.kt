package com.example.appestudos.features.intro.ui

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.ui.theme.LocalThemeManager
import androidx.compose.material.icons.outlined.WbIncandescent
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import com.example.appestudos.features.flashcards.model.GrupoResponseDTO
import com.example.appestudos.features.flashcards.model.getIconFromPath
import com.example.appestudos.features.auth.data.ApiClient
import com.example.appestudos.features.auth.data.ApiService

@Composable
fun HomeScreen(navController: NavController) {
    val scaffoldState = rememberScaffoldState()
    val navigationBarInsets = WindowInsets.navigationBars.asPaddingValues()
    val isDark = !MaterialTheme.colors.isLight
    val apiService = remember { ApiService(ApiClient.httpClient) }
    val userId = UserManager.getCurrentUser()?.id
    var grupos by remember { mutableStateOf<List<GrupoResponseDTO>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Buscar grupos da API
    LaunchedEffect(userId) {
        loading = true
        try {
            val response = apiService.listarGruposPorUsuario(userId ?: 0)
            println("API grupos: $response") // Debug print
            if (response.success && response.data != null) {
                grupos = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        loading = false
    }

    val gruposPublicos = grupos.filter { it.idUsuario == null }
    val gruposPrivados = grupos.filter { it.idUsuario == userId }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        scaffoldState = scaffoldState,
        bottomBar = { MyButtonBar(navController, isDark) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createGroup") },
                contentColor = Color.White,
                backgroundColor = if (isDark) Color(0xFF27391C) else Color(0xFF255F38)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Adicionar Grupo",
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
            NameProfile(navController, isDark)
            Publicos(isDark)
            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .heightIn(min = 120.dp, max = 240.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(gruposPublicos) { group ->
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(80.dp)
                                .background(if (isDark) Color(0xFF27391C) else Color(0xFF255F38), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    val encoded = java.net.URLEncoder.encode(group.descricao, "UTF-8")
                                    navController.navigate("flashcardGroup/${group.id}/$encoded/public")
                                },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = getIconFromPath(group.path),
                                contentDescription = group.descricao,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = group.descricao,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Privados(isDark)
            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .heightIn(min = 120.dp, max = 240.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(gruposPrivados) { group ->
                        Column(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(80.dp)
                                .background(if (isDark) Color(0xFF27391C) else Color(0xFF255F38), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    val encoded = java.net.URLEncoder.encode(group.descricao, "UTF-8")
                                    navController.navigate("flashcardGroup/${group.id}/$encoded/private")
                                },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = getIconFromPath(group.path),
                                contentDescription = group.descricao,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = group.descricao,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            // Mensagem se nenhum grupo for encontrado
            if (!loading && gruposPublicos.isEmpty() && gruposPrivados.isEmpty()) {
                Text("Nenhum grupo encontrado.", color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(navigationBarInsets.calculateBottomPadding()))
        }
    }
}

@Composable
fun NameProfile(navController: NavController, isDark: Boolean) {
    var showMenu by remember { mutableStateOf(false) }
    val currentUser = UserManager.getCurrentUser()
    val themeManager = LocalThemeManager.current
    var shouldClearUser by remember { mutableStateOf(false) }

    @Composable
    fun DayNightToggleSimple(
        isDark: Boolean,
        onToggle: () -> Unit
    ) {
        val trackColor = if (!isDark) Color.Black else Color(0xFFFF5722)
        val width = 48.dp
        val height = 24.dp

        val thumbOffset by animateDpAsState(if (!isDark) 4.dp else (width - height + 4.dp))

        Box(
            Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(trackColor)
                .clickable { onToggle() }
        ) {
            Box(
                Modifier
                    .size(height - 8.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                if (!isDark) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Lua",
                        tint = trackColor,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Brightness2,
                        contentDescription = "Sol",
                        tint = trackColor,
                        modifier = Modifier
                            .size(12.dp)
                            .rotate(-40f)
                            .align(Alignment.Center)
                            .graphicsLayer {scaleX = -1f}
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Studyfy",
                color = if (isDark) Color.White else Color.Black,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )
            Box {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(35.dp)
                        .clickable { showMenu = true },
                    tint = if (isDark) Color.White else Color.Unspecified
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    modifier = Modifier
                        .width(200.dp)
                        .background(if (isDark) Color(0xFF222222) else Color.White, RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        DropdownMenuItem(onClick = { /* No action */ }) {
                            Text(
                                text = "Olá, ${currentUser?.nome ?: "Erro"}",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                        Divider(color = if (isDark) Color.Gray else Color.LightGray)
                        DropdownMenuItem(onClick = {
                            navController.navigate("performance")
                        }) {
                            Text("Pefil", color = if (isDark) Color.White else Color.Black)
                        }
                        DropdownMenuItem(
                            onClick = { themeManager.toggleDarkMode() },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Modo Escuro",
                                    color = if (isDark) Color.White else Color.Black
                                )
                                DayNightToggleSimple(
                                    isDark = themeManager.isDarkMode,
                                    onToggle = { themeManager.toggleDarkMode() }
                                )
                            }
                        }
                        Divider(color = if (isDark) Color.Gray else Color.LightGray)
                        DropdownMenuItem(onClick = {
                            navController.navigate("LoginScreen") {
                                popUpTo(0) { inclusive = true }
                            }
                            shouldClearUser = true
                        }) {
                            Text("Sair", color = if (isDark) Color.White else Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (shouldClearUser) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            UserManager.clearCurrentUser()
        }
    }
}


@Composable
fun Privados(isDark: Boolean) {
    Text(
        "Grupos Privados",
        color = if (isDark) Color.White else Color.Black,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
fun Publicos(isDark: Boolean) {
    Text(
        "Grupos Públicos",
        color = if (isDark) Color.White else Color.Black,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
fun MyButtonBar(navController: NavController, isDark: Boolean) {
    val items = listOf(
        BottomMenuItem("Home", Icons.Outlined.Home),
        BottomMenuItem("Lupa", Icons.Outlined.WbIncandescent),
        BottomMenuItem("Maps", Icons.Outlined.LocationOn)
    )
    var selected by remember { mutableStateOf("Home") }

    BottomAppBar(
        cutoutShape = CircleShape,
        backgroundColor = if (isDark) Color.Black else Color.White,
        elevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                selected = selected == item.label,
                onClick = {
                    selected = item.label
                    when (item.label) {
                        "Home" -> navController.navigate("HomeScreen") {
                            popUpTo("HomeScreen") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "Lupa" -> navController.navigate("search") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "Maps" -> navController.navigate("map") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(if (item.label == "Lupa") 180f else 0f),
                        tint = if (isDark) Color.White else Color.Black
                    )
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
