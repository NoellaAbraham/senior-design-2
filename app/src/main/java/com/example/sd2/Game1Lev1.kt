package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Game1Lev1 : ComponentActivity() {

    private lateinit var faceImage: ImageView
    private lateinit var emotionText: TextView
    private lateinit var flipPrompt: TextView
    private lateinit var continueButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var cardView: CardView
    private lateinit var menuIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private val emotions = arrayOf("Happy", "Sad", "Angry", "Surprised", "Disgust", "Fear")
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var isImageVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_lev1)

        faceImage = findViewById(R.id.faceImage)
        emotionText = findViewById(R.id.emotionText)
        flipPrompt = findViewById(R.id.flipPrompt)
        continueButton = findViewById(R.id.continueButton)
        nextButton = findViewById(R.id.scaredans)
        cardView = findViewById(R.id.flipCard)
        menuIcon = findViewById(R.id.menuIcon)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)

        updateEmotion()

        continueButton.setOnClickListener {
            showNextEmotion()
        }

        nextButton.setOnClickListener {
            startActivity(Intent(this, Game1Lev1Test::class.java))
            val userID = (application as MyApp).userID
            saveProgressToDatabase(userID, 1, 1, 10)
        }

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

        cardView.setOnClickListener {
            flipCard()
        }
    }

    private fun flipCard() {
        val flipOut = AnimationUtils.loadAnimation(this, R.anim.flip_out)
        val flipIn = AnimationUtils.loadAnimation(this, R.anim.flip_in)

        cardView.startAnimation(flipOut)
        flipOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation) {
                if (isImageVisible) {
                    faceImage.visibility = View.GONE
                    emotionText.visibility = View.VISIBLE
                } else {
                    faceImage.visibility = View.VISIBLE
                    emotionText.visibility = View.GONE
                }
                isImageVisible = !isImageVisible
                cardView.startAnimation(flipIn)
            }
        })
    }

    private fun showNextEmotion() {
        currentIndex = (currentIndex + 1) % emotions.size
        updateEmotion()
    }

    private fun updateEmotion() {
        val emotion = emotions[currentIndex]
        val drawableId = resources.getIdentifier(emotion.lowercase(), "drawable", packageName)
        val audioId = resources.getIdentifier("${emotion.lowercase()}_audio", "raw", packageName)

        faceImage.setImageResource(drawableId)
        emotionText.text = emotion.uppercase()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, audioId)
        mediaPlayer?.playbackParams = PlaybackParams().setSpeed(0.75f)
        mediaPlayer?.setVolume(1.2f, 1.2f)
        mediaPlayer?.start()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}
