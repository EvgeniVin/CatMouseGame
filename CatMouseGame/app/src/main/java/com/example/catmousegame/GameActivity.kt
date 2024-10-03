package com.example.catmousegame

import android.content.Context
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.atan2
import kotlin.random.Random
import androidx.constraintlayout.widget.ConstraintLayout

class GameActivity : AppCompatActivity() {

    private lateinit var gameScreen: ConstraintLayout
    private lateinit var hitCountView: TextView
    private lateinit var missCountView: TextView
    private val handler = Handler()
    private var hitCount = 0
    private var missCount = 0
    private var backgroundIndex = 1  // Индекс текущего фона
    private var mouseSize = 400f
    private var mouseSpeed = 5f
    private var mouseCount = 1f

    private val mice = mutableListOf<ImageView>()
    private lateinit var squeakRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        hitCountView = findViewById(R.id.hitCountView)
        missCountView = findViewById(R.id.missCountView)
        gameScreen = findViewById(R.id.gameScreen)

        loadSettings()

        if (savedInstanceState == null) {
            backgroundIndex = loadNextBackgroundIndex()
        } else {
            backgroundIndex = savedInstanceState.getInt("backgroundIndex", 1)
            hitCount = savedInstanceState.getInt("hitCount", 0)
            missCount = savedInstanceState.getInt("missCount", 0)
            updateHitCount()
            updateMissCount()
        }

        setGameBackground()

        gameScreen.setOnClickListener {
            missCount++
            updateMissCount()
            playMissSound()
        }

        for (i in 0 until mouseCount.toInt()) {
            handler.postDelayed({ createMouse() }, Random.nextLong(500, 3000))
        }

        playSqueakSound()
    }

    private fun createMouse() {
        val mouse = ImageView(this)
        val mouseImageRes = resources.getIdentifier(
            "mouse${Random.nextInt(1, 8)}", "drawable", packageName
        )
        mouse.setImageResource(mouseImageRes)
        mouse.layoutParams = ConstraintLayout.LayoutParams(mouseSize.toInt(), mouseSize.toInt())

        mouse.setOnClickListener {
            hitCount++
            updateHitCount()
            playHitSound()
            gameScreen.removeView(mouse)
            mice.remove(mouse)

            val delay = Random.nextLong(1000, 5000)
            handler.postDelayed({
                createMouse()
            }, delay)
        }
        gameScreen.addView(mouse)
        mice.add(mouse)

        startMouseMovement(mouse)
    }

    private fun startMouseMovement(mouse: ImageView) {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val screenWidth = size.x
        val screenHeight = size.y

        // Устанавливаем мышь полностью вне экрана
        val startX = if (Random.nextBoolean()) {
            if (Random.nextBoolean()) -mouse.width.toFloat() else (screenWidth + mouse.width).toFloat()
        } else {
            Random.nextInt(0, screenWidth - mouse.width).toFloat()
        }

        val startY = if (startX == -mouse.width.toFloat() || startX == (screenWidth + mouse.width).toFloat()) {
            Random.nextInt(0, screenHeight - mouse.height).toFloat()
        } else {
            if (Random.nextBoolean()) -mouse.height.toFloat() else (screenHeight + mouse.height).toFloat()
        }

        mouse.x = startX
        mouse.y = startY

        moveMouseToRandomPoint(mouse)
    }

    private fun moveMouse(mouse: ImageView, endX: Float, endY: Float) {
        val startX = mouse.x
        val startY = mouse.y
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble()).toFloat()

        // ПОВОРОТ МЫШИ В СТОРОНУ ДВИЖЕНИЯ
        val targetRotation = angle * (180 / Math.PI.toFloat())
        mouse.animate()
            .rotation(targetRotation)
            .setDuration(200) // Установите продолжительность поворота
            .start()

        val duration = (5000 / mouseSpeed).toLong()

        mouse.animate()
            .x(endX)
            .y(endY)
            .setDuration(duration)
            .withEndAction {
                // После достижения конечной точки, вызываем новую точку
                moveMouseToRandomPoint(mouse)
            }
            .start()
    }

    private fun moveMouseToRandomPoint(mouse: ImageView) {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val screenWidth = size.x
        val screenHeight = size.y

        // Генерация новой случайной точки на экране
        val endX = Random.nextInt(0, screenWidth - mouse.width).toFloat()
        val endY = Random.nextInt(0, screenHeight - mouse.height).toFloat()

        moveMouse(mouse, endX, endY)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        mouseSize = prefs.getFloat("mouseSize", 400f)
        mouseSpeed = prefs.getFloat("mouseSpeed", 5f)
        mouseCount = prefs.getFloat("mouseCount", 1f)
    }

    private fun setGameBackground() {
        val backgroundRes = resources.getIdentifier(
            "game_background$backgroundIndex", "drawable", packageName
        )
        gameScreen.setBackgroundResource(backgroundRes)
    }

    private fun loadNextBackgroundIndex(): Int {
        val prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val currentIndex = prefs.getInt("backgroundIndex", 1)
        val nextIndex = if (currentIndex == 3) 1 else currentIndex + 1

        prefs.edit().putInt("backgroundIndex", nextIndex).apply()
        return currentIndex
    }

    private fun updateHitCount() {
        hitCountView.text = "$hitCount"
    }

    private fun playHitSound() {
        val hitSoundRes = resources.getIdentifier("hit1", "raw", packageName)
        val mediaPlayer = MediaPlayer.create(this, hitSoundRes)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(100)
    }

    private fun updateMissCount() {
        missCountView.text = "$missCount"
    }

    private fun playMissSound() {
        val hitSoundRes = resources.getIdentifier("hit2", "raw", packageName)
        val mediaPlayer = MediaPlayer.create(this, hitSoundRes)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }

    private fun playSqueakSound() {
        squeakRunnable = Runnable {
            val squeakRes = resources.getIdentifier(
                "mouse_squeak${Random.nextInt(1, 4)}", "raw", packageName
            )
            val mediaPlayer = MediaPlayer.create(this, squeakRes)

            mediaPlayer.setOnCompletionListener {
                it.release()
            }

            mediaPlayer.start()

            handler.postDelayed(squeakRunnable, Random.nextLong(5000, 15000))
        }

        handler.post(squeakRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("backgroundIndex", backgroundIndex)
        outState.putInt("hitCount", hitCount)
        outState.putInt("missCount", missCount)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        backgroundIndex = savedInstanceState.getInt("backgroundIndex", 1)
        hitCount = savedInstanceState.getInt("hitCount", 0)
        missCount = savedInstanceState.getInt("missCount", 0)
        updateHitCount()
        updateMissCount()

        setGameBackground()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(squeakRunnable)
    }

    override fun onResume() {
        super.onResume()
        playSqueakSound()
    }

    override fun onStop() {
        super.onStop()
        val databaseHelper = DatabaseHelper(this)
        databaseHelper.addRecord(hitCount, missCount)
    }
}