package com.example.sd2

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class Game1Lev2 : AppCompatActivity() {
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var proceedButton: Button

    private val emotions = arrayOf("Happy", "Sad", "Angry", "Surprised", "Disgust","Fear")
    private var currentIndex = 0
    private var emotionsVisited = 0

    private var mediaPlayer: MediaPlayer? = null

   // private lateinit var bgmMediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1_lev2)

        viewFlipper = findViewById(R.id.viewFlipper)
        nextButton = findViewById(R.id.sadButt)
        prevButton = findViewById(R.id.happyButt)
        imageView = findViewById(R.id.imageView2)
        textView = findViewById(R.id.textView)
        proceedButton = findViewById(R.id.proceedButton)

        // Flip animation (both files)
        val inAnimation = AnimationUtils.loadAnimation(this, R.anim.flip_in)
        val outAnimation = AnimationUtils.loadAnimation(this, R.anim.flip_out)
        viewFlipper.inAnimation = inAnimation
        viewFlipper.outAnimation = outAnimation

        viewFlipper.setOnClickListener {
            viewFlipper.showNext()
            checkEmotionIndex()
        }

        nextButton.setOnClickListener {
            showNextEmotion()
        }

        prevButton.setOnClickListener {
            showPreviousEmotion()
        }

        proceedButton.setOnClickListener {
            goToNextActivity(this, Game1Lev2Test::class.java)

            val progress = 10
            val userID = (application as MyApp).userID

            saveProgressToDatabase(userID, 1, 3, progress)
        }

        // Set initial emotion
        updateEmotion()

        //Initialize background music
      //  bgmMediaPlayer = MediaPlayer.create(this, R.raw.bgm1)
      //  bgmMediaPlayer.isLooping = true
      //  bgmMediaPlayer.setVolume(0.05f, 0.05f) // Set the volume level here (0.5f for half volume, 1.0f is full volume)
      //  bgmMediaPlayer.start()
    }

    private fun showNextEmotion() {
        // Increment index
        currentIndex = (currentIndex + 1) % emotions.size
        // Update emotion
        updateEmotion()
    }

    private fun showPreviousEmotion() {
        // Decrement index
        currentIndex = (currentIndex - 1 + emotions.size) % emotions.size
        // Update emotion
        updateEmotion()
    }

    private fun updateEmotion() {
        val emotion = emotions[currentIndex]
        val drawableId = resources.getIdentifier(("human_" + emotion.toLowerCase()), "drawable", packageName)
        imageView.setImageResource(drawableId)
        textView.text = emotion

        // Play corresponding audio
        playAudio(emotion)

        // Increment emotionsVisited
        emotionsVisited++
    }

    private fun playAudio(emotion: String) {
        mediaPlayer?.stop() // Stop previously playing audio
        mediaPlayer?.release() // Release previous MediaPlayer instance
        val audioResourceId = resources.getIdentifier(emotion.toLowerCase() + "_audio", "raw", packageName)
        mediaPlayer = MediaPlayer.create(this, audioResourceId)
        mediaPlayer?.setVolume(1.5f, 1.5f) // Set the volume level here (0.5f for half volume, 1.0f is full volume)
        mediaPlayer?.start()
    }

    private fun checkEmotionIndex() {
        if (emotionsVisited >= emotions.size) {
            proceedButton.visibility = View.VISIBLE
        } else {
            proceedButton.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}