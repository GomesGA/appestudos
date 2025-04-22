package com.example.appestudos

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars

data class Flashcard(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val isPublic: Boolean
)

@Composable
fun intro(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    
    // Sample flashcards
    val publicFlashcards = remember {
        listOf(
            Flashcard(1, "Programação", Icons.Filled.Code, true),
            Flashcard(2, "Matemática", Icons.Filled.Calculate, true),
            Flashcard(3, "Ciências", Icons.Filled.Science, true),
            Flashcard(4, "História", Icons.Filled.Book, true),
            Flashcard(5, "Geografia", Icons.Filled.Public, true),
            Flashcard(6, "Línguas", Icons.Filled.Language, true),
            Flashcard(7, "Tecnologia", Icons.Filled.Computer, true),
            Flashcard(8, "Negócios", Icons.Filled.Business, true)
        )
    }

    val privateFlashcards = remember {
        listOf(
            Flashcard(5, "Inglês", Icons.Filled.Code, false),
            Flashcard(6, "Espanhol", Icons.Filled.DesktopWindows, false)
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding.calculateTopPadding()),
        bottomBar = {
            MyButtonBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createGroup") },
                contentColor = Color.White,
                backgroundColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Adicionar",
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp)
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
            Publicos()
            FlashcardList(flashcards = publicFlashcards, isPublic = true, navController = navController)
            Privados()
            FlashcardList(flashcards = privateFlashcards, isPublic = false, navController = navController)
            // Add bottom spacing to ensure content doesn't get hidden behind navigation bar
            Spacer(modifier = Modifier.height(navigationBarPadding.calculateBottomPadding()))
        }
    }
}

@Composable
fun NameProfile(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(imageVector = Icons.Filled.Menu,
            contentDescription = null,
            modifier = Modifier
                .size(35.dp)
                .clickable {  }

        )

        Spacer(modifier = Modifier.weight(1f))

        Image(imageVector = Icons.Filled.Person,
            contentDescription = null,
            modifier = Modifier
                .width(35.dp)
                .height(35.dp)
                .clickable {  }

        )
    }
}

@Composable
fun Publicos(){

    Text("Flashcard Públicos",
        color = Color.Black,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    )

}

@Composable
fun FlashcardList(flashcards: List<Flashcard>, isPublic: Boolean, navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Primeira linha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            flashcards.take(4).forEach { flashcard ->
                FlashcardItem(
                    flashcard = flashcard,
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .clickable {
                            if (isPublic) {
                                val groupName = java.net.URLEncoder.encode(flashcard.title, "UTF-8")
                                navController.navigate("flashcardGroup/${flashcard.id}/${groupName}")
                            }
                        }
                )
            }
        }
        
        // Segunda linha (se for público)
        if (isPublic) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                flashcards.drop(4).take(4).forEach { flashcard ->
                    FlashcardItem(
                        flashcard = flashcard,
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .clickable {
                                val groupName = java.net.URLEncoder.encode(flashcard.title, "UTF-8")
                                navController.navigate("flashcardGroup/${flashcard.id}/${groupName}")
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(
    flashcard: Flashcard,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(85.dp)
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = flashcard.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = flashcard.title,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
fun Privados(){

    Text("Flashcard Privados",
        color = Color.Black,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    )
}

@Composable
fun MyButtonBar(navController: NavController){
    val bottomMenuItemsList = prepareButtomMenu()
    val contextForToast = LocalContext.current.applicationContext
    var selectItem by remember {
        mutableStateOf("home")
    }
    BottomAppBar (
        cutoutShape = CircleShape,
        backgroundColor = Color.White,
        elevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        bottomMenuItemsList.forEachIndexed{ index, bottomMenuItem ->
            BottomNavigationItem(
                selected = selectItem==bottomMenuItem.label,
                onClick = {
                    selectItem=bottomMenuItem.label
                    when (bottomMenuItem.label) {
                        "Maps" -> navController.navigate("map")
                        else -> Toast.makeText(contextForToast, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                    }
                },
                icon = {
                    Icon(
                        imageVector = bottomMenuItem.icon,
                        contentDescription = bottomMenuItem.label,
                        tint = Color.Black,
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                    )
                },
                alwaysShowLabel = true,
                enabled = true
            )
        }
    }
}

data class BottomMenuItem(val label: String, val icon: ImageVector)

@Composable
fun prepareButtomMenu(): List<BottomMenuItem>{
    val bottomMenuItemList = arrayListOf<BottomMenuItem>()

    bottomMenuItemList.add(
        BottomMenuItem(
            label = "Home",
            icon = Icons.Filled.Home
        )
    )

    bottomMenuItemList.add(
        BottomMenuItem(
            label = "Lupa",
            icon = Icons.Filled.Search
        )
    )

    bottomMenuItemList.add(
        BottomMenuItem(
            label = "Maps",
            icon = Icons.Filled.LocationOn
        )
    )

    return bottomMenuItemList
}

@Preview
@Composable
fun IntroPreview() {
    intro(navController = rememberNavController())
}