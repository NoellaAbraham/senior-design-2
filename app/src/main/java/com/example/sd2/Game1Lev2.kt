package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Game1Lev2 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var proceedButton: Button

    private val emotions = arrayOf("Happy", "Sad", "Angry", "Surprised", "Disgust", "Fear")
    private var currentIndex = 0
    private var emotionsVisited = 0

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_lev2)

        // Drawer setup
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

        // UI References
        viewFlipper = findViewById(R.id.viewFlipper)
        nextButton = findViewById(R.id.nextButton)
        prevButton = findViewById(R.id.prevButton)
        imageView = findViewById(R.id.imageView2)
        textView = findViewById(R.id.textView)
        proceedButton = findViewById(R.id.proceedButton)

        // Flip animations
        val inAnim = AnimationUtils.loadAnimation(this, R.anim.flip_in)
        val outAnim = AnimationUtils.loadAnimation(this, R.anim.flip_out)
        viewFlipper.inAnimation = inAnim
        viewFlipper.outAnimation = outAnim

        viewFlipper.setOnClickListener {
            viewFlipper.showNext()
        }

        nextButton.setOnClickListener {
            showNextEmotion()
        }

        prevButton.setOnClickListener {
            showPreviousEmotion()
        }

        proceedButton.setOnClickListener {
            goToNextActivity(this, Game1Lev2Test::class.java)
            val userID = (application as MyApp).userID
            saveProgressToDatabase(userID, 1, 3, 10)
        }

        updateEmotion()
    }

    private fun showNextEmotion() {
        currentIndex = (currentIndex + 1) % emotions.size
        updateEmotion()
    }

    private fun showPreviousEmotion() {
        currentIndex = (currentIndex - 1 + emotions.size) % emotions.size
        updateEmotion()
    }

    private fun updateEmotion() {
        val emotion = emotions[currentIndex]
        val drawableId = resources.getIdentifier("human_" + emotion.lowercase(), "drawable", packageName)
        val audioId = resources.getIdentifier("${emotion.lowercase()}_audio", "raw", packageName)

        imageView.setImageResource(drawableId)
        textView.text = emotion.uppercase()

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, audioId)
        mediaPlayer?.setVolume(1.2f, 1.2f)
        mediaPlayer?.start()

        emotionsVisited++
        proceedButton.visibility = if (emotionsVisited >= emotions.size) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}
