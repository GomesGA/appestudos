package com.example.appestudos.features.flashcards.repo

import android.content.ContentValues
import android.content.Context
import com.example.appestudos.features.flashcards.db.FlashcardDbHelper
import com.example.appestudos.features.flashcards.viewmodel.FlashcardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlashcardRepository(context: Context) {
    private val dbHelper = FlashcardDbHelper(context)

    suspend fun add(f: FlashcardEntity) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("groupId",   f.groupId)
            put("groupTitle",f.groupTitle)
            put("iconName",  f.iconName)
            put("title",     f.title)
            put("content",   f.content)
        }
        db.insert("flashcards", null, cv)
        db.close()
    }

    suspend fun getByGroup(groupId: Int): List<FlashcardEntity> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "flashcards",
            arrayOf("id","groupId","groupTitle","iconName","title", "content"),
            "groupId = ?",
            arrayOf(groupId.toString()),
            null, null, null
        )
        val list = mutableListOf<FlashcardEntity>()
        while (cursor.moveToNext()) {
            list += FlashcardEntity(
                id         = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                groupId    = cursor.getInt(cursor.getColumnIndexOrThrow("groupId")),
                groupTitle = cursor.getString(cursor.getColumnIndexOrThrow("groupTitle")),
                iconName   = cursor.getString(cursor.getColumnIndexOrThrow("iconName")),
                title      = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                content    = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            )
        }
        cursor.close()
        db.close()
        list
    }
}
