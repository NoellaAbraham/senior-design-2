package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class Game1Lev1Test : AppCompatActivity() {

    private lateinit var timeTaken: TextView
    private lateinit var triesText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView
    private lateinit var pauseButton: ImageButton
    private lateinit var continueButton: ImageButton

    private var startTime: Long = 0L
    private var pauseTime: Long = 0L
    private var totalPausedTime: Long = 0L
    private var isTimerRunning = true
    private val handler = Handler(Looper.getMainLooper())

    private var numberOfTries = 0
    private var mistakes = 0

    private val images = listOf(
        R.drawable.happy,
        R.drawable.sad,
        R.drawable.angry,
        R.drawable.surprised,
        R.drawable.disgust,
        R.drawable.fear
    ).shuffled()

    private lateinit var imageView: ImageView
    private lateinit var happyButton: Button
    private lateinit var sadButton: Button
    private lateinit var angryButton: Button
    private lateinit var surprisedButton: Button
    private lateinit var disgustButton: Button
    private lateinit var fearButton: Button

    private var currentIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_lev1_test)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)
        pauseButton = findViewById(R.id.pauseButton)
        continueButton = findViewById(R.id.continueButton)
        imageView = findViewById(R.id.imageView2)
        timeTaken = findViewById(R.id.timeTaken)
        triesText = findViewById(R.id.triesText)

        happyButton = findViewById(R.id.happyButt)
        sadButton = findViewById(R.id.sadButt)
        angryButton = findViewById(R.id.angryButt)
        surprisedButton = findViewById(R.id.surprisedButt)
        disgustButton = findViewById(R.id.disgustButt)
        fearButton = findViewById(R.id.fearButt)

        menuIcon.setOnClickListener {
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

        setButtonListeners()

        pauseButton.setOnClickListener {
            pauseTime = System.currentTimeMillis()
            isTimerRunning = false
        }

        continueButton.setOnClickListener {
            if (!isTimerRunning) {
                totalPausedTime += System.currentTimeMillis() - pauseTime
                isTimerRunning = true
                updateTimer()
            }
        }

        startTime = System.currentTimeMillis()
        updateTimer()
        nextImage()
    }

    private fun updateTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedMillis = currentTime - startTime - totalPausedTime
                    val minutes = (elapsedMillis / 1000) / 60
                    val seconds = (elapsedMillis / 1000) % 60
                    timeTaken.text = String.format("Time: %02d:%02d", minutes, seconds)
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun setButtonListeners() {
        happyButton.setOnClickListener { handleGuess("happy", R.raw.happy_audio) }
        sadButton.setOnClickListener { handleGuess("sad", R.raw.sad_audio) }
        angryButton.setOnClickListener { handleGuess("angry", R.raw.angry_audio) }
        surprisedButton.setOnClickListener { handleGuess("surprised", R.raw.surprised_audio) }
        disgustButton.setOnClickListener { handleGuess("disgust", R.raw.disgust_audio) }
        fearButton.setOnClickListener { handleGuess("fear", R.raw.fear_audio) }
    }

    private fun handleGuess(guess: String, audioRes: Int) {
        numberOfTries++
        triesText.text = "No. of tries: $numberOfTries"
        val correct = getImageName(images[currentIndex]) == guess
        playSound(audioRes)

        if (correct) {
            if (currentIndex + 1 >= images.size) {
                saveScoreToDatabase()
                startActivity(Intent(this, Game1Lev2::class.java).apply {
                    putExtra("CURRENT_LEVEL", "Game1Lev1Test")
                })
                finish()
            } else {
                nextImage()
            }
        } else {
            mistakes++
        }
    }

    private fun playSound(audioResource: Int) {
        val mediaPlayer = MediaPlayer.create(this, audioResource)
        mediaPlayer?.setVolume(1.5f, 1.5f)
        val playbackParams = PlaybackParams().apply { speed = 0.75f }
        mediaPlayer.playbackParams = playbackParams
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }

    private fun nextImage() {
        currentIndex++
        if (currentIndex < images.size) {
            imageView.setImageResource(images[currentIndex])
        }
    }

    private fun getImageName(imageResId: Int): String {
        return resources.getResourceEntryName(imageResId)
    }

    private fun saveScoreToDatabase() {
        val endTime = System.currentTimeMillis()
        val totalTimeMillis = endTime - startTime - totalPausedTime
        val totalSecs = (totalTimeMillis / 1000).toInt()
        val formattedTime = String.format("%02d:%02d", totalSecs / 60, totalSecs % 60)

        val userID = (application as MyApp).userID
        val gameID = 1
        val levelID = 1

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"

                val postData =
                    "userID=$userID&gameID=$gameID&levelID=$levelID&mistakes=$mistakes&time=$formattedTime"
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Game1Lev1Test", "Score saved successfully")
                } else {
                    Log.e("Game1Lev1Test", "Failed to save score")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}
