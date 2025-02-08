package com.example.sd2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class Game2Lev1 : AppCompatActivity() {
    private lateinit var videoView: VideoView
    private lateinit var continueButton: Button
    private lateinit var happyButton: Button
    private lateinit var angryButton: Button
    private lateinit var scaredButton: Button
    private lateinit var sadButton: Button

    private var currentIndex = -1
    private var mistakes = 0

    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_lev1)

        showContinueButton()

        videoView = findViewById(R.id.videoView)
        happyButton = findViewById(R.id.happy_ans)
        angryButton = findViewById(R.id.angry_ans)
        scaredButton = findViewById(R.id.scared_ans)
        sadButton = findViewById(R.id.sad_ans)

        // Directly specify the video file name within the URI string
        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.happy_lev1}")
        videoView.setVideoURI(offlineUri)

        setupMediaControls()

        videoView.setOnCompletionListener {

        }

        happyButton.setOnClickListener { checkAnswer("happy") }
        angryButton.setOnClickListener { checkAnswer("angry") }
        scaredButton.setOnClickListener { checkAnswer("scared") }
        sadButton.setOnClickListener { checkAnswer("sad") }

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
            val intent = Intent(this, Game2lev12::class.java)
            startActivity(intent)

            saveScoreToDatabase()

            val progress = 10;
            val userID = (application as MyApp).userID

            saveProgressToDatabase(userID, 2, 7, progress)
            finish()
        }
    }

    private fun checkAnswer(selectedOption: String) {
        val correctOption = getCorrectOptionFromRawFileName()
        Log.d("Game2Lev1", "Correct option: $correctOption, Selected option: $selectedOption")



        if (selectedOption == correctOption) {
            // Set the selected button to green
            setOtherButtonsGray(selectedOption)

            val greenColor = 0xFF00FF00.toInt() // Green color
            when (selectedOption) {
                "happy" -> happyButton.setBackgroundColor(greenColor)
                "angry" -> angryButton.setBackgroundColor(greenColor)
                "scared" -> scaredButton.setBackgroundColor(greenColor)
                "sad" -> sadButton.setBackgroundColor(greenColor)
            }
            // Set other buttons to gray

        } else {
            // Show toast message to try again
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            mistakes++;
        }
    }

    private fun saveScoreToDatabase() {

        endTime = System.currentTimeMillis()
        val timeTaken = endTime - startTime

        val minutes = (timeTaken / 1000) / 60
        val seconds = (timeTaken / 1000) % 60

        // Format time as mm:ss
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userID = (application as MyApp).userID
                println(userID)
                val gameID = 2 // Assuming gameID for game1 is 1
                val levelID = 7 // Assuming levelID for level1 is 1

                val url = URL("http://192.168.56.1/seniordes/g1l1test.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.doOutput = true
                urlConnection.requestMethod = "POST"

                // Construct POST data
                val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&mistakes=$mistakes&time=$formattedTime"
                println(postData)
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Score saved successfully
                    println("Score saved successfully")
                } else {
                    // Error saving score
                    println("Error saving score")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setOtherButtonsGray(selectedOption: String) {
        val buttons = listOf(happyButton, angryButton, scaredButton, sadButton)
        for (button in buttons) {
            if (button.tag != selectedOption) {
                button.setBackgroundColor(0xFFCCCCCC.toInt()) // Gray color
            }
        }
    }

    private fun getCorrectOptionFromRawFileName(): String {

        return "happy" // Dummy value for now
    }
}