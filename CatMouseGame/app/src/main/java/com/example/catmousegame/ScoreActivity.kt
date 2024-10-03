package com.example.catmousegame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.util.Log
import android.widget.Button

class ScoreActivity : AppCompatActivity() {

    private lateinit var statisticsTable: TableLayout
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        statisticsTable = findViewById(R.id.statisticsTable)
        backButton = findViewById(R.id.backButton)

        // Загрузка данных из БД (здесь должно быть ваше подключение к БД)
        val records = loadGameRecords() // Метод для загрузки записей из БД

        // Вывод данных в таблицу
        displayRecords(records)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun displayRecords(records: List<GameRecord>) {
        // Удаляем предыдущие строки, если есть
        statisticsTable.removeViewsInLayout(1, statisticsTable.childCount - 1)

        // Если записей меньше 10, добавляем символ "-" в пустые строки
        val totalRecords = if (records.size < 10) 10 else records.size

        for (index in 0 until totalRecords) {
            val row = TableRow(this)

            // Проверяем, есть ли запись для текущего индекса
            val record = if (index < records.size) records[index] else null

            // Создаем TextView для каждого столбца и выравниваем содержимое по центру
            val numberText = TextView(this).apply {
                text = (index + 1).toString()
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER // Выравнивание по центру
                setTextColor(Color.BLACK) // Цвет текста черный
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }

            val percentageText = TextView(this).apply {
                text = record?.let { String.format("%.2f%%", it.percentage) } ?: "-"
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER // Выравнивание по центру
                setTextColor(Color.BLACK) // Цвет текста черный
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            }

            val hitsText = TextView(this).apply {
                text = record?.hits?.toString() ?: "-" // Показываем количество попаданий или "-" если записи нет
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER // Выравнивание по центру
                setTextColor(Color.BLACK) // Цвет текста черный
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            }

            val missesText = TextView(this).apply {
                text = record?.misses?.toString() ?: "-" // Показываем количество промахов или "-" если записи нет
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER // Выравнивание по центру
                setTextColor(Color.BLACK) // Цвет текста черный
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            }

            val dateText = TextView(this).apply {
                text = record?.let { formatDateTime(it.datetime) } ?: "-"
                setPadding(0, 8, 0, 8)
                gravity = Gravity.CENTER // Выравнивание по центру
                setTextColor(Color.BLACK) // Цвет текста черный
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3.5f)
            }

            // Добавляем все элементы в строку
            row.addView(numberText)
            row.addView(percentageText)
            row.addView(hitsText)
            row.addView(missesText)
            row.addView(dateText)

            // Добавляем строку в таблицу
            statisticsTable.addView(row)
        }
    }

    // Форматирование даты
    private fun formatDateTime(dateTime: String): String {
        return if (dateTime.isNotEmpty() && dateTime.contains(" ")) {
            val parts = dateTime.split(" ")
            val date = parts[0].split("-")
            val time = parts[1].split(":")

            String.format("%04d-%02d-%02d %02d:%02d",
                date[0].toInt(),
                date[1].toInt(),
                date[2].toInt(),
                time[0].toInt(),
                time[1].toInt()
            )
        } else {
            "-"
        }
    }


    private fun loadGameRecords(): List<GameRecord> {
        val records = mutableListOf<GameRecord>()
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Запрос для получения записей с сортировкой по проценту, попаданиям и дате
        val cursor = db.rawQuery(
            "SELECT * FROM game_records ORDER BY datetime DESC LIMIT 10",
            null
        )

        // Чтение данных из курсора
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex("id")
                val hitsIndex = cursor.getColumnIndex("hits")
                val missesIndex = cursor.getColumnIndex("misses")
                val percentageIndex = cursor.getColumnIndex("percentage")
                val dateIndex = cursor.getColumnIndex("datetime")

                // Проверка, что индексы не равны -1
                if (idIndex >= 0 && hitsIndex >= 0 && missesIndex >= 0 && percentageIndex >= 0 && dateIndex >= 0) {
                    val id = cursor.getInt(idIndex)
                    val hits = cursor.getInt(hitsIndex)
                    val misses = cursor.getInt(missesIndex)
                    val percentage = cursor.getFloat(percentageIndex)
                    val date = cursor.getString(dateIndex)

                    records.add(GameRecord(id, hits, misses, percentage, date))
                } else {
                    // Логирование ошибки, если один из индексов не найден
                    Log.e("GameRecord", "Не удалось найти один из столбцов в курсоре.")
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }
}