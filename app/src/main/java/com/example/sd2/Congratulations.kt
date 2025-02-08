package com.example.sd2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Congratulations : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_congratulations)

        // Initialize MediaPlayer with the background music
        mediaPlayer = MediaPlayer.create(this, R.raw.kids_cheering)
        mediaPlayer.start()

        val currentLevel = intent.getStringExtra("CURRENT_LEVEL")

        val nextLevel = getNextLevel(currentLevel)

        val nextLevelButton = findViewById<Button>(R.id.buttonNext)
        nextLevelButton.setOnClickListener {
            // Stop and release MediaPlayer when the activity navigates away
            // mediaPlayer.stop()
            // mediaPlayer.release()

            val nextLevelIntent = when (nextLevel) {
                "Congratulations" -> Intent(this, Congratulations::class.java)
                else -> Intent(this, Class.forName("com.example.sd2.$nextLevel"))
            }
            nextLevelIntent.putExtra("CURRENT_LEVEL", nextLevel)
            startActivity(nextLevelIntent)
            finish()
        }
    }

    private fun getNextLevel(currentLevel: String?): String {
        return when (currentLevel) {
            "Game1Lev1Test" -> "Game1Lev2"
            "Game2Lev14" -> "Game2Lev2"
            "Game2Lev2" -> "Game2Lev31"
            "Game3Lev1" -> "Game3Lev2"
            "Game3Lev2" -> "Game3Lev3"
            else -> ""
        }
    }

    override fun onStop() {
        // Pause and release MediaPlayer when the activity is stopped
        mediaPlayer.pause()
        mediaPlayer.release()
        super.onStop()
    }
}
