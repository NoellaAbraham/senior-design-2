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

class Game2Lev31 : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var continueButton: Button
    private lateinit var happyButton: Button
    private lateinit var angryButton: Button
    private lateinit var scaredButton: Button
    private lateinit var sadButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_lev31)

        showContinueButton()

        videoView = findViewById(R.id.videoView)
        happyButton = findViewById(R.id.happy_ans)
        angryButton = findViewById(R.id.angry_ans)
        scaredButton = findViewById(R.id.scared_ans)
        sadButton = findViewById(R.id.sad_ans)

        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.lev3_1}")
        videoView.setVideoURI(offlineUri)

        setupMediaControls()

        videoView.setOnCompletionListener {

        }

        val imageButton5 = findViewById<ImageButton>(R.id.home_button)
        imageButton5.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
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

    private fun showContinueButton() {
        continueButton = findViewById(R.id.continueBtn)
        continueButton.visibility = View.VISIBLE

        happyButton.visibility = View.GONE
        angryButton.visibility = View.GONE
        scaredButton.visibility = View.GONE
        sadButton.visibility = View.GONE

        continueButton.setOnClickListener {
            val intent = Intent(this, Game2Lev32::class.java)
            intent.putExtra("CURRENT_LEVEL", "Game2Lev31")
            startActivity(intent)



            val progress = 10;
            val userID = (application as MyApp).userID

            saveProgressToDatabase(userID, 2, 12, progress)
            finish()
        }
    }
}