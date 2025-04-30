package com.example.sd2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class Game2lev12 : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var continueButton: Button
    private lateinit var happyButton: Button
    private lateinit var angryButton: Button
    private lateinit var scaredButton: Button
    private lateinit var sadButton: Button
    private lateinit var timeTakenText: TextView
    private lateinit var triesText: TextView

    private var mistakes = 0
    private var secondsElapsed = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var startTime: Long = 0
    private var endTime: Long = 0

    private var isCorrect = false
    private var isVideoCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_lev12)

        videoView = findViewById(R.id.videoView)
        happyButton = findViewById(R.id.happy_ans)
        angryButton = findViewById(R.id.angry_ans)
        scaredButton = findViewById(R.id.scared_ans)
        sadButton = findViewById(R.id.sad_ans)
        continueButton = findViewById(R.id.continueBtn)
        timeTakenText = findViewById(R.id.timeTaken)
        triesText = findViewById(R.id.triesText)

        startTimer()

        val offlineUri: Uri = Uri.parse("android.resource://$packageName/${R.raw.angry_lev1}")
        videoView.setVideoURI(offlineUri)

        setupMediaControls()

        videoView.setOnCompletionListener {
            isVideoCompleted = true
            if (isCorrect) showContinueButton()
        }

        happyButton.setOnClickListener { checkAnswer("happy") }
        angryButton.setOnClickListener { checkAnswer("angry") }
        scaredButton.setOnClickListener { checkAnswer("scared") }
        sadButton.setOnClickListener { checkAnswer("sad") }

        startTime = System.currentTimeMillis()
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

    private fun checkAnswer(selectedOption: String) {
        val correctOption = getCorrectOptionFromRawFileName()
        Log.d("Game2lev12", "Correct: $correctOption, Selected: $selectedOption")

        if (selectedOption == correctOption) {
            isCorrect = true

            // Highlight selected correct button green
            when (selectedOption) {
                "happy" -> happyButton.setBackgroundColor(0xFF00FF00.toInt())
                "angry" -> angryButton.setBackgroundColor(0xFF00FF00.toInt())
                "scared" -> scaredButton.setBackgroundColor(0xFF00FF00.toInt())
                "sad" -> sadButton.setBackgroundColor(0xFF00FF00.toInt())
            }

            // Set other buttons to gray
            setOtherButtonsGray(selectedOption)

            if (isVideoCompleted) {
                showContinueButton()
            }

        } else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show()
            mistakes++
            triesText.text = "No. of tries: $mistakes"
        }
    }

    private fun setOtherButtonsGray(correct: String) {
        val gray = 0xFFCCCCCC.toInt()
        if (correct != "happy") happyButton.setBackgroundColor(gray)
        if (correct != "angry") angryButton.setBackgroundColor(gray)
        if (correct != "scared") scaredButton.setBackgroundColor(gray)
        if (correct != "sad") sadButton.setBackgroundColor(gray)
    }

    private fun showContinueButton() {
        stopTimer()
        continueButton.visibility = View.VISIBLE

        happyButton.visibility = View.GONE
        angryButton.visibility = View.GONE
        scaredButton.visibility = View.GONE
        sadButton.visibility = View.GONE

        continueButton.setOnClickListener {
            val intent = Intent(this, Game2Lev13::class.java)
            startActivity(intent)

            saveScoreToDatabase()

            val userID = (application as MyApp).userID
            saveProgressToDatabase(userID, 2, 8, 10)
            finish()
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
                val levelID = 8 // This is for 'angry' (Game 2 Lev 1.2)

                val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"

                val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&mistakes=$mistakes&time=$formattedTime"
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("Game2lev12", "Score saved")
                } else {
                    Log.e("Game2lev12", "Score save failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private fun getCorrectOptionFromRawFileName(): String {
        return "angry"
    }
}
