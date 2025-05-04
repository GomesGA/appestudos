package com.example.appestudos.features.flashcards.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconFromPath(path: String): ImageVector {
    return when (path) {
        "androidx.compose.material.icons.filled.Code" -> Icons.Filled.Code
        "androidx.compose.material.icons.filled.Calculate" -> Icons.Filled.Calculate
        "androidx.compose.material.icons.filled.Science" -> Icons.Filled.Science
        "androidx.compose.material.icons.filled.Book" -> Icons.Filled.Book
        "androidx.compose.material.icons.filled.Public" -> Icons.Filled.Public
        "androidx.compose.material.icons.filled.Language" -> Icons.Filled.Language
        "androidx.compose.material.icons.filled.Computer" -> Icons.Filled.Computer
        "androidx.compose.material.icons.filled.Business" -> Icons.Filled.Business
        else -> Icons.Filled.Help // ícone padrão
    }
} 