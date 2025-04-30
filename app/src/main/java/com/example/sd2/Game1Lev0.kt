package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Game1Lev0 : ComponentActivity() {

    private lateinit var timeTaken: TextView
    private lateinit var triesText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private var timerHandler = Handler()
    private var timerStartTime = 0L
    private var timeBuff = 0L
    private var updateTime = 0L
    private var isTimerRunning = true

    private var numberOfTries = 0

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTime = SystemClock.uptimeMillis() - timerStartTime
            val totalTime = timeBuff + updateTime

            val seconds = (totalTime / 1000).toInt() % 60
            val minutes = (totalTime / (1000 * 60)).toInt()
            timeTaken.text = String.format("Time: %02d:%02d", minutes, seconds)

            if (isTimerRunning) {
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game1_lev0)

        findViewById<Button>(R.id.continueBtn).setOnClickListener {
            val intent = Intent(this, Game1Lev1::class.java)
            startActivity(intent)
            finish() // Optional: close current screen
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timeTaken = findViewById(R.id.timeTaken)
        triesText = findViewById(R.id.triesText)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)

        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, WelcomeActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardTest::class.java))
                R.id.nav_chatbot -> startActivity(Intent(this, Chatbot::class.java))
                R.id.nav_appointments -> startActivity(Intent(this, DoctorsAppointment::class.java))
                R.id.nav_resources -> startActivity(Intent(this, ResourcesActivity::class.java))
                R.id.nav_feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.nav_logout -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val moodButtons = listOf(
            R.id.imageButton, R.id.imageButton2, R.id.imageButton3,
            R.id.imageButton4, R.id.imageButton5, R.id.imageButton6
        )

        val images = listOf(
            R.drawable.angry, R.drawable.happy, R.drawable.sad,
            R.drawable.surprised, R.drawable.disgust, R.drawable.fear
        )

        val sounds = listOf(
            R.raw.angry_audio, R.raw.happy_audio, R.raw.sad_audio,
            R.raw.surprised_audio, R.raw.disgust_audio, R.raw.fear_audio
        )

        val imageView = findViewById<ImageView>(R.id.imageView)

        moodButtons.forEachIndexed { index, id ->
            findViewById<ImageButton>(id).setOnClickListener {
                imageView.setImageResource(images[index])
                playSound(sounds[index])
                numberOfTries++
                triesText.text = "No. of tries: $numberOfTries"
            }
        }

        findViewById<ImageButton>(R.id.pauseButton).setOnClickListener {
            pauseTimer()
        }

        findViewById<ImageButton>(R.id.continueButton).setOnClickListener {
            resumeTimer()
        }

        findViewById<ImageButton>(R.id.scaredans).setOnClickListener {
            val intent = Intent(this, Game1Lev1::class.java)
            startActivity(intent)

            val progress = 20
            val userID = (application as MyApp).userID
            saveProgressToDatabase(userID, 1, 2, progress)
        }

        startTimer()
    }

    private fun startTimer() {
        timerStartTime = SystemClock.uptimeMillis()
        timerHandler.postDelayed(timerRunnable, 0)
        isTimerRunning = true
    }

    private fun pauseTimer() {
        timeBuff += SystemClock.uptimeMillis() - timerStartTime
        timerHandler.removeCallbacks(timerRunnable)
        isTimerRunning = false
    }

    private fun resumeTimer() {
        timerStartTime = SystemClock.uptimeMillis()
        timerHandler.postDelayed(timerRunnable, 0)
        isTimerRunning = true
    }

    private fun playSound(audioResource: Int) {
        val mediaPlayer = MediaPlayer.create(this, audioResource)
        mediaPlayer?.setVolume(1.0f, 1.0f)
        try {
            val playbackParams = PlaybackParams().apply { speed = 0.75f }
            mediaPlayer.playbackParams = playbackParams
        } catch (e: Exception) {
            // fallback for older Android versions
        }
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }
}
