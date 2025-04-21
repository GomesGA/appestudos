package com.example.appestudos

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun intro(navController: NavController) {
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()

    Scaffold (bottomBar = {
        MyButtonBar()
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { /*Todo*/ },
            contentColor = Color.White,
            backgroundColor = Color.Black)
        {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Adicionar",
                modifier = Modifier
                    .height(30.dp)
                    .width(30.dp)
            )
        }
    }){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues = it)
        ){
            NameProfile()
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
fun MyButtonBar(){
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
                    Toast.makeText(contextForToast, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
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