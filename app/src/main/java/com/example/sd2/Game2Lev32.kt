package com.example.sd2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class Game2Lev32 : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var continueButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_lev32)

        continueButton = findViewById(R.id.continueButton)
        continueButton.visibility = View.VISIBLE

        continueButton.setOnClickListener {
            val intent = Intent(this, Congratulations2::class.java)
            intent.putExtra("CURRENT_LEVEL", "Game2Lev31")
            startActivity(intent)



            val progress = 10;
            val userID = (application as MyApp).userID

            saveProgressToDatabase(userID, 2, 12, progress)
            finish()
        }

        videoView = findViewById(R.id.videoView)

        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.lev3_2}")
        videoView.setVideoURI(offlineUri)

        setupMediaControls()

        val imageButton5 = findViewById<ImageButton>(R.id.home_button)

        imageButton5.setOnClickListener {

            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)

        }



        videoView.setOnCompletionListener {
            val intent = Intent(this, Congratulations2::class.java)
            startActivity(intent)

            val progress = 10;
            val userID = (application as MyApp).userID

            saveProgressToDatabase(userID, 2, 13, progress)
            finish()
        }
    }

    private fun setupMediaControls() {
        findViewById<ImageButton>(R.id.play).setOnClickListener {
            if (!videoView.isPlaying) {
                videoView.start()
            }
        }

        findViewById<ImageButton>(R.id.pause).setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
            }
        }

        findViewById<ImageButton>(R.id.rewind).setOnClickListener {
            videoView.seekTo(videoView.currentPosition - 5000)
        }

        findViewById<ImageButton>(R.id.forward).setOnClickListener {
            videoView.seekTo(videoView.currentPosition + 5000)
        }
    }

}