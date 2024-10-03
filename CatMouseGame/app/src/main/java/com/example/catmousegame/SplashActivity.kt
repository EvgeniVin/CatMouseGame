package com.example.catmousegame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ProgressBar
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)

        // Переход на главный экран через 2 секунды
        val splashThread = Thread {
            Thread.sleep(2000)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        splashThread.start()

        // Заполнение индикатора загрузки (пример)
        fillProgressBar()
    }

    private fun fillProgressBar() {
        val handler = Handler()
        val runnable = object : Runnable {
            var progressStatus = 0

            override fun run() {
                if (progressStatus < 100) {
                    progressStatus++
                    progressBar.progress = progressStatus
                    handler.postDelayed(this, 10) // Задержка между обновлениями (50 мс)
                }
            }
        }
        handler.post(runnable)
    }
}
