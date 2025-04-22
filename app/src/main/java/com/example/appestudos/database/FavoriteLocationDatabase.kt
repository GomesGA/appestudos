package com.example.appestudos.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.appestudos.FavoriteLocation
import com.google.android.gms.maps.model.LatLng

class FavoriteLocationDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "favorite_locations.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "favorite_locations"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addFavoriteLocation(name: String, location: LatLng): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LATITUDE, location.latitude)
            put(COLUMN_LONGITUDE, location.longitude)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllFavoriteLocations(): List<FavoriteLocation> {
        val locations = mutableListOf<FavoriteLocation>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE))
                val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                
                locations.add(
                    FavoriteLocation(
                        id = id,
                        name = name,
                        location = LatLng(latitude, longitude)
                    )
                )
            }
        }
        return locations
    }

    fun deleteFavoriteLocation(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getFavoriteLocationsCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        return if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
    }

    fun updateFavoriteLocation(id: Int, name: String, location: LatLng) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LATITUDE, location.latitude)
            put(COLUMN_LONGITUDE, location.longitude)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
} 