package com.example.catmousegame

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private lateinit var sizeSlider: Slider
    private lateinit var speedSlider: Slider
    private lateinit var countSlider: Slider
    private lateinit var cancelButton: Button
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sizeSlider = findViewById(R.id.sizeSlider)
        speedSlider = findViewById(R.id.speedSlider)
        countSlider = findViewById(R.id.countSlider)
        cancelButton = findViewById(R.id.cancelButton)
        confirmButton = findViewById(R.id.confirmButton)

        // Загружаем актуальные настройки
        loadSettings()

        // Обработка нажатия на "Отмена" (просто возвращаемся в меню)
        cancelButton.setOnClickListener {
            finish() // Возврат без изменений
        }

        // Обработка нажатия на "Подтвердить" (сохраняем настройки и возвращаемся)
        confirmButton.setOnClickListener {
            saveSettings()
            finish() // Возврат в меню
        }
    }

    // Загружаем актуальные настройки из SharedPreferences
    private fun loadSettings() {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        sizeSlider.value = prefs.getFloat("mouseSize", 400f)
        speedSlider.value = prefs.getFloat("mouseSpeed", 5f)
        countSlider.value = prefs.getFloat("mouseCount", 1f)
    }

    // Сохраняем изменения в SharedPreferences
    private fun saveSettings() {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putFloat("mouseSize", sizeSlider.value)
            putFloat("mouseSpeed", speedSlider.value)
            putFloat("mouseCount", countSlider.value)
            apply()
        }
    }
}