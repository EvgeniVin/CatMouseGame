package com.example.catmousegame

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "game_stats.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "game_records"
        private const val COLUMN_ID = "id"
        private const val COLUMN_HITS = "hits"
        private const val COLUMN_MISSES = "misses"
        private const val COLUMN_PERCENTAGE = "percentage"
        private const val COLUMN_DATETIME = "datetime"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создаем таблицу для хранения статистики
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_HITS INTEGER, $COLUMN_MISSES INTEGER, "
                + "$COLUMN_PERCENTAGE REAL, $COLUMN_DATETIME TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Удаляем таблицу, если она существует, и создаем заново
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Метод для добавления записи в базу данных
    fun addRecord(hits: Int, misses: Int) {
        val db = writableDatabase
        val values = ContentValues()
        val percentage = (hits.toFloat() / (hits + misses) * 100).takeIf { it.isFinite() } ?: 0f

        // Получаем текущую дату и время в формате "YYYY-MM-DD HH:MM:SS"
        val currentDateTime = getCurrentDateTime()

        values.put(COLUMN_HITS, hits)
        values.put(COLUMN_MISSES, misses)
        values.put(COLUMN_PERCENTAGE, percentage)
        values.put(COLUMN_DATETIME, currentDateTime)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // Метод для получения текущей даты и времени в формате "YYYY-MM-DD HH:MM:SS"
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

// Модель для записи игры
data class GameRecord(val id: Int, val hits: Int, val misses: Int, val percentage: Float, val datetime: String)