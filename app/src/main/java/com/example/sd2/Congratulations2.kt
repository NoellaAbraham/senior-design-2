package com.example.sd2

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer

class Congratulations2 : AppCompatActivity() {
    private lateinit var dashButton: Button

    // private lateinit var bgmMediaPlayer: MediaPlayer
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_congratulations2)

        // Initialize MediaPlayer with the background music
        mediaPlayer = MediaPlayer.create(this, R.raw.kids_cheering)
        mediaPlayer.start()

        dashButton = findViewById(R.id.buttonNext)

        dashButton.setOnClickListener{
            goToNextActivity(this, Dashboard::class.java)
        }
    }

    override fun onStop() {
        super.onStop()
        // Pause and release MediaPlayer when the activity is stopped
        mediaPlayer.stop()
        mediaPlayer.release()
       // bgmMediaPlayer.stop() // Stop background music when activity is stopped
    }
}