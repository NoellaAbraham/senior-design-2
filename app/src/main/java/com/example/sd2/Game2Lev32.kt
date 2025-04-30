package com.example.sd2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
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
import java.util.concurrent.TimeUnit

class Game2Lev32 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private lateinit var videoView: VideoView
    private lateinit var continueButton: Button
    private lateinit var sadButton: Button
    private lateinit var happyButton: Button
    private lateinit var angryButton: Button
    private lateinit var scaredButton: Button
    private lateinit var timeTakenText: TextView
    private lateinit var triesText: TextView

    private var tries = 0
    private var secondsElapsed = 0
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var isCorrect = false
    private var isVideoDone = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_lev32)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
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

        videoView = findViewById(R.id.videoView)
        sadButton = findViewById(R.id.sad_ans)
        happyButton = findViewById(R.id.happy_ans)
        angryButton = findViewById(R.id.angry_ans)
        scaredButton = findViewById(R.id.scared_ans)
        continueButton = findViewById(R.id.continueBtn)
        timeTakenText = findViewById(R.id.timeTaken)
        triesText = findViewById(R.id.triesText)

        continueButton.visibility = View.GONE
        startTime = System.currentTimeMillis()
        startTimer()

        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.lev3_2}")
        videoView.setVideoURI(offlineUri)

        setupMediaControls()

        sadButton.setOnClickListener { checkAnswer("A") }
        happyButton.setOnClickListener { checkAnswer("B") }
        angryButton.setOnClickListener { checkAnswer("C") }
        scaredButton.setOnClickListener { checkAnswer("D") }

        videoView.setOnCompletionListener {
            isVideoDone = true
            if (isCorrect) {
                stopTimer()
                showContinueButton()
            }
        }
    }

    private fun checkAnswer(selected: String) {
        val correctAnswer = "B"
        val green = 0xFF00FF00.toInt()
        val gray = 0xFFB0B0B0.toInt()

        if (selected == correctAnswer) {
            isCorrect = true

            when (selected) {
                "A" -> sadButton.setBackgroundColor(green)
                "B" -> happyButton.setBackgroundColor(green)
                "C" -> angryButton.setBackgroundColor(green)
                "D" -> scaredButton.setBackgroundColor(green)
            }

            if (selected != "A") sadButton.setBackgroundColor(gray)
            if (selected != "B") happyButton.setBackgroundColor(gray)
            if (selected != "C") angryButton.setBackgroundColor(gray)
            if (selected != "D") scaredButton.setBackgroundColor(gray)

            if (isVideoDone) {
                stopTimer()
                showContinueButton()
            }

        } else {
            tries++
            triesText.text = "No. of tries: $tries"
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContinueButton() {
        continueButton.visibility = View.VISIBLE
        sadButton.visibility = View.GONE
        happyButton.visibility = View.GONE
        angryButton.visibility = View.GONE
        scaredButton.visibility = View.GONE

        continueButton.setOnClickListener {
            saveScoreToDatabase()
            val intent = Intent(this, Game3Lev2::class.java)
            intent.putExtra("CURRENT_LEVEL", "Game2Lev32")
            startActivity(intent)
            finish()
        }
    }

    private fun setupMediaControls() {
        findViewById<ImageButton>(R.id.play).setOnClickListener {
            if (!videoView.isPlaying) videoView.start()
        }

        findViewById<ImageButton>(R.id.pause).setOnClickListener {
            if (videoView.isPlaying) videoView.pause()
        }

        findViewById<ImageButton>(R.id.rewind).setOnClickListener {
            videoView.seekTo(videoView.currentPosition - 5000)
        }

        findViewById<ImageButton>(R.id.forward).setOnClickListener {
            videoView.seekTo(videoView.currentPosition + 5000)
        }
    }

    private fun startTimer() {
        runnable = object : Runnable {
            override fun run() {
                secondsElapsed++
                val timeStr = String.format(
                    "Time: %02d:%02d",
                    TimeUnit.SECONDS.toMinutes(secondsElapsed.toLong()),
                    secondsElapsed % 60
                )
                timeTakenText.text = timeStr
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
    }

    private fun saveScoreToDatabase() {
        endTime = System.currentTimeMillis()
        val totalTimeMillis = endTime - startTime
        val totalSecs = (totalTimeMillis / 1000).toInt()
        val formattedTime = String.format("%02d:%02d", totalSecs / 60, totalSecs % 60)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userID = (application as MyApp).userID
                val gameID = 2
                val levelID = 32

                val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"

                val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&mistakes=$tries&time=$formattedTime"
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Game2Lev32", "Score saved")
                } else {
                    Log.e("Game2Lev32", "Score save failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
