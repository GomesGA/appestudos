package com.example.appestudos.features.flashcards.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class FlashcardGroup(val id: Int, val title: String, val icon: ImageVector) {
    PROGRAMACAO(1, "Programação", Icons.Filled.Code),
    MATEMATICA(2, "Matemática", Icons.Filled.Calculate),
    CIENCIAS(3, "Ciências", Icons.Filled.Science),
    HISTORIA(4, "História ", Icons.Filled.Book),
    GEOGRAFIA(5, "Geografia", Icons.Filled.Public),
    LINGUAS(6, "Línguas", Icons.Filled.Language),
    TECNOLOGIA(7, "Tecnologia", Icons.Filled.Computer),
    NEGOCIOS(8, "Negócios", Icons.Filled.Business)
}