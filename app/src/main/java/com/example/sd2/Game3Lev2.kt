package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class Game3Lev2 : AppCompatActivity() {
    private lateinit var buttons: List<ImageButton>
    private lateinit var cards: MutableList<MemoryCard>
    private var indexOfFirstSelectedCard: Int? = null
    private var indexOfSecondSelectedCard: Int? = null
    private var isClickable = true
    private var matchedPairs = 0 // Variable to keep track of matched pairs

    private var mistakes = 0

    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_lev2)

        val imageButton7 = findViewById<ImageButton>(R.id.home_button)

        imageButton7.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        val emotions = listOf(
            R.drawable.happy_side, R.drawable.happy_word,
            R.drawable.sad_side, R.drawable.sad_word,
            R.drawable.surprised_side, R.drawable.surprised_word,
            R.drawable.angry_side, R.drawable.angry_word
        )

        // Shuffle the emotions
        val shuffledEmotions = emotions.shuffled()

        val frontImage = R.drawable.front_side // Front of all cards

        buttons = listOf(
            findViewById(R.id.front_side1), findViewById(R.id.front_side2),
            findViewById(R.id.front_side3), findViewById(R.id.front_side4),
            findViewById(R.id.front_side5), findViewById(R.id.front_side6),
            findViewById(R.id.front_side7), findViewById(R.id.front_side8)
        )

        cards = mutableListOf()

        for ((index, _) in shuffledEmotions.withIndex()) {
            cards.add(MemoryCard(frontImage, shuffledEmotions[index], false))
            buttons[index].setImageResource(frontImage)
            buttons[index].setOnClickListener { onCardClicked(index) }
        }

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
                isClickable = false // Prevent further clicks
                checkForMatch()
            }
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
                val gameID = 3 // Assuming gameID for game1 is 1
                val levelID = 15 // Assuming levelID for level1 is 1

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

    private fun checkForMatch() {
        if (indexOfFirstSelectedCard != null && indexOfSecondSelectedCard != null) {
            val firstCard = cards[indexOfFirstSelectedCard!!]
            val secondCard = cards[indexOfSecondSelectedCard!!]

            val match = if (
                (firstCard.back == R.drawable.happy_side && secondCard.back == R.drawable.happy_word) ||
                (firstCard.back == R.drawable.happy_word && secondCard.back == R.drawable.happy_side) ||
                (firstCard.back == R.drawable.sad_side && secondCard.back == R.drawable.sad_word) ||
                (firstCard.back == R.drawable.sad_word && secondCard.back == R.drawable.sad_side) ||
                (firstCard.back == R.drawable.angry_side && secondCard.back == R.drawable.angry_word) ||
                (firstCard.back == R.drawable.angry_word && secondCard.back == R.drawable.angry_side) ||
                (firstCard.back == R.drawable.surprised_side && secondCard.back == R.drawable.surprised_word) ||
                (firstCard.back == R.drawable.surprised_word && secondCard.back == R.drawable.surprised_side)
            ) {
                true
            } else {
                false
            }

            if (match) {
                // Match found
                Toast.makeText(this, "Match found!", Toast.LENGTH_SHORT).show()
                firstCard.isMatched = true
                secondCard.isMatched = true
                matchedPairs++ // Increment matched pairs
                if (matchedPairs == cards.size / 2) { // Check if all pairs are matched
                    goToLev3Activity()
                }
                isClickable = true // Allow clicks again
            } else {
                // No match
                mistakes++;
                Handler().postDelayed({
                    firstCard.isFaceUp = false
                    secondCard.isFaceUp = false
                    updateViews()
                    isClickable = true // Allow clicks again
                }, 2000)
            }
            indexOfFirstSelectedCard = null
            indexOfSecondSelectedCard = null
        }
    }

    private fun updateViews() {
        for ((index, card) in cards.withIndex()) {
            val button = buttons[index]
            if (card.isMatched) {
                button.isEnabled = false // Deactivate matched cards
            }
            button.setImageResource(if (card.isFaceUp || card.isMatched) card.back else card.front)
        }
    }

    private fun resetGame(shuffledEmotions: List<Int>) {
        shuffledEmotions.forEachIndexed { index, emotion ->
            cards[index].back = emotion
            cards[index].isFaceUp = false
            cards[index].isMatched = false
        }
        updateViews()
    }

    private fun goToLev3Activity() {
        val intent = Intent(this, Game3Lev3::class.java)
        startActivity(intent)

        val progress = 50;
        val userID = (application as MyApp).userID

        saveProgressToDatabase(userID, 3, 15, progress)
        finish()
    }

    data class MemoryCard(var front: Int, var back: Int, var isFaceUp: Boolean = false, var isMatched: Boolean = false)
}