package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
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

class Game1Lev2Test : AppCompatActivity() {

    private lateinit var timeTaken: TextView
    private lateinit var triesText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private lateinit var pauseButton: ImageButton
    private lateinit var continueButton: ImageButton

    private lateinit var imageView: ImageView
    private lateinit var happyButton: Button
    private lateinit var sadButton: Button
    private lateinit var angryButton: Button
    private lateinit var surprisedButton: Button
    private lateinit var disgustButton: Button
    private lateinit var fearButton: Button

    private var startTime: Long = 0
    private var handler = Handler(Looper.getMainLooper())
    private var currentIndex = -1
    private var mistakes = 0
    private var numberOfTries = 0
    private var isPaused = false

    private val images = listOf(
        R.drawable.human_happy,
        R.drawable.human_sad,
        R.drawable.human_angry,
        R.drawable.human_surprised,
        R.drawable.human_disgust,
        R.drawable.human_fear
    ).shuffled()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_lev2_test)

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)
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

        // Timer and tries
        timeTaken = findViewById(R.id.timeTaken)
        triesText = findViewById(R.id.triesText)

        // Pause & Play
        pauseButton = findViewById(R.id.pauseButton)
        continueButton = findViewById(R.id.continueButton)

        pauseButton.setOnClickListener { isPaused = true }
        continueButton.setOnClickListener {
            if (isPaused) {
                isPaused = false
                updateTimer()
            }
        }

        // Emotion buttons
        imageView = findViewById(R.id.imageView2)
        happyButton = findViewById(R.id.happyButt)
        sadButton = findViewById(R.id.sadButt)
        angryButton = findViewById(R.id.angryButt)
        surprisedButton = findViewById(R.id.surprisedButt)
        disgustButton = findViewById(R.id.disgustButt)
        fearButton = findViewById(R.id.fearButt)

        happyButton.setOnClickListener { checkAnswer("human_happy", R.raw.happy_audio) }
        sadButton.setOnClickListener { checkAnswer("human_sad", R.raw.sad_audio) }
        angryButton.setOnClickListener { checkAnswer("human_angry", R.raw.angry_audio) }
        surprisedButton.setOnClickListener { checkAnswer("human_surprised", R.raw.surprised_audio) }
        disgustButton.setOnClickListener { checkAnswer("human_disgust", R.raw.disgust_audio) }
        fearButton.setOnClickListener { checkAnswer("human_fear", R.raw.fear_audio) }

        // Game Start
        startTime = System.currentTimeMillis()
        updateTimer()
        nextImage()
    }

    private fun updateTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isPaused) {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val minutes = (elapsedMillis / 1000) / 60
                    val seconds = (elapsedMillis / 1000) % 60
                    timeTaken.text = String.format("Time: %02d:%02d", minutes, seconds)
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun nextImage() {
        currentIndex = (currentIndex + 1) % images.size
        imageView.setImageResource(images[currentIndex])
    }

    private fun checkAnswer(guess: String, audioRes: Int) {
        numberOfTries++
        triesText.text = "No. of tries: $numberOfTries"

        val correct = getImageName(images[currentIndex]) == guess
        playSound(audioRes)

        if (correct) {
            if (currentIndex == images.size - 1) {
                saveScoreToDatabase()
                val userID = (application as MyApp).userID
                saveProgressToDatabase(userID, 1, 3, 40)
                goToNextActivity(this, WelcomeActivity::class.java)
            } else {
                nextImage()
            }
        } else {
            mistakes++
        }
    }

    private fun getImageName(imageResId: Int): String {
        return resources.getResourceEntryName(imageResId)
    }

    private fun playSound(audioResource: Int) {
        val mediaPlayer = MediaPlayer.create(this, audioResource)
        mediaPlayer?.setVolume(1.5f, 1.5f)
        mediaPlayer.playbackParams = PlaybackParams().apply { speed = 0.8f }
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { it.release() }
    }

    private fun saveScoreToDatabase() {
        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - startTime
        val formattedTime = String.format("%02d:%02d", (timeTaken / 1000) / 60, (timeTaken / 1000) % 60)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userID = (application as MyApp).userID
                val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"
                val postData =
                    "userID=$userID&gameID=1&levelID=2&mistakes=$mistakes&time=$formattedTime"
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    println("Score saved successfully")
                } else {
                    println("Failed saving score")
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
