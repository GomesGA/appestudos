package com.example.appestudos.features.flashcards.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "flashcards.db"
private const val DB_VERSION = 2

class FlashcardDbHelper(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
      CREATE TABLE flashcards (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        groupId INTEGER NOT NULL,
        groupTitle TEXT NOT NULL,
        iconName TEXT NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL
      )
    """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // adiciona apenas a coluna 'content' sem apagar dados existentes
            db.execSQL(
                "ALTER TABLE flashcards ADD COLUMN content TEXT NOT NULL DEFAULT ''"
            )
        }
        // para futuras versões, basta encadear mais condições:
        // if (oldVersion < 3) { db.execSQL("ALTER TABLE flashcards ADD COLUMN novaColuna ...") }
    }
}
