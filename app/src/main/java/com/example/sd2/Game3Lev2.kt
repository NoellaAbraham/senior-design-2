package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class Game3Lev2 : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private lateinit var buttons: List<ImageButton>
    private lateinit var cards: MutableList<MemoryCard>
    private var indexOfFirstSelectedCard: Int? = null
    private var indexOfSecondSelectedCard: Int? = null
    private var isClickable = true
    private var matchedPairs = 0
    private var mistakes = 0
    private var startTime: Long = 0
    private var endTime: Long = 0

    private lateinit var timeTextView: TextView
    private lateinit var triesTextView: TextView

    private val timerHandler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val minutes = (elapsed / 1000) / 60
            val seconds = (elapsed / 1000) % 60
            timeTextView.text = "Time: %02d:%02d".format(minutes, seconds)
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_lev2)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)
        timeTextView = findViewById(R.id.timeTaken)
        triesTextView = findViewById(R.id.triesText)

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

        val emotions = listOf(
            R.drawable.happy_side, R.drawable.happy_word,
            R.drawable.sad_side, R.drawable.sad_word,
            R.drawable.surprised_side, R.drawable.surprised_word,
            R.drawable.angry_side, R.drawable.angry_word
        ).shuffled()

        val frontImage = R.drawable.front_side
        buttons = listOf(
            findViewById(R.id.front_side1), findViewById(R.id.front_side2),
            findViewById(R.id.front_side3), findViewById(R.id.front_side4),
            findViewById(R.id.front_side5), findViewById(R.id.front_side6),
            findViewById(R.id.front_side7), findViewById(R.id.front_side8)
        )

        cards = mutableListOf()
        for ((index, _) in emotions.withIndex()) {
            cards.add(MemoryCard(frontImage, emotions[index], false))
            buttons[index].setImageResource(frontImage)
            buttons[index].setOnClickListener { onCardClicked(index) }
        }

        startTime = System.currentTimeMillis()
        timerHandler.post(timerRunnable)
        triesTextView.text = "No. of tries: 0"
    }

    private fun onCardClicked(position: Int) {
        val card = cards[position]
        if (isClickable && !card.isFaceUp) {
            card.isFaceUp = true
            updateViews()
            if (indexOfFirstSelectedCard == null) {
                indexOfFirstSelectedCard = position
            } else {
                indexOfSecondSelectedCard = position
                isClickable = false
                checkForMatch()
            }
        }
    }

    private fun checkForMatch() {
        val first = indexOfFirstSelectedCard ?: return
        val second = indexOfSecondSelectedCard ?: return

        val firstCard = cards[first]
        val secondCard = cards[second]

        val match = (firstCard.back == R.drawable.happy_side && secondCard.back == R.drawable.happy_word) ||
                (firstCard.back == R.drawable.happy_word && secondCard.back == R.drawable.happy_side) ||
                (firstCard.back == R.drawable.sad_side && secondCard.back == R.drawable.sad_word) ||
                (firstCard.back == R.drawable.sad_word && secondCard.back == R.drawable.sad_side) ||
                (firstCard.back == R.drawable.angry_side && secondCard.back == R.drawable.angry_word) ||
                (firstCard.back == R.drawable.angry_word && secondCard.back == R.drawable.angry_side) ||
                (firstCard.back == R.drawable.surprised_side && secondCard.back == R.drawable.surprised_word) ||
                (firstCard.back == R.drawable.surprised_word && secondCard.back == R.drawable.surprised_side)

        if (match) {
            Toast.makeText(this, "Match found!", Toast.LENGTH_SHORT).show()
            firstCard.isMatched = true
            secondCard.isMatched = true
            matchedPairs++
            if (matchedPairs == cards.size / 2) {
                goToNextLevel()
            }
            isClickable = true
        } else {
            mistakes++
            triesTextView.text = "No. of tries: $mistakes"
            Handler().postDelayed({
                firstCard.isFaceUp = false
                secondCard.isFaceUp = false
                updateViews()
                isClickable = true
            }, 2000)
        }

        indexOfFirstSelectedCard = null
        indexOfSecondSelectedCard = null
    }

    private fun updateViews() {
        for ((index, card) in cards.withIndex()) {
            val button = buttons[index]
            val currentDrawable = if (card.isFaceUp || card.isMatched) card.back else card.front
            if ((button.tag as? Int) != currentDrawable) {
                button.setImageResource(currentDrawable)
                button.tag = currentDrawable
            }
            button.isEnabled = !card.isMatched
        }
    }


    private fun goToNextLevel() {
        timerHandler.removeCallbacks(timerRunnable)
        saveScoreToDatabase()
        val userID = (application as MyApp).userID
        saveProgressToDatabase(userID, 3, 15, 100)
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun saveScoreToDatabase() {
        endTime = System.currentTimeMillis()
        val timeTaken = endTime - startTime
        val minutes = (timeTaken / 1000) / 60
        val seconds = (timeTaken / 1000) % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userID = (application as MyApp).userID
                val gameID = 3
                val levelID = 15

                val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"

                val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&mistakes=$mistakes&time=$formattedTime"
                conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    println("Score saved successfully")
                } else {
                    println("Failed to save score")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class MemoryCard(var front: Int, var back: Int, var isFaceUp: Boolean = false, var isMatched: Boolean = false)
}
