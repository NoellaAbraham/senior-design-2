package com.example.sd2

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class  Dashboard : ComponentActivity() {
    private lateinit var game1_butt: Button
    private lateinit var game2_butt: Button
    private lateinit var game3_butt: Button
    private lateinit var pieChart1: PieChart
    private lateinit var pieChart2: PieChart
    private lateinit var pieChart3: PieChart

    private lateinit var cardView2: CardView
    private lateinit var cardView3: CardView

    private var gameProgress: MutableMap<Int, Float> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        game1_butt = findViewById(R.id.game01Go)
        game2_butt = findViewById(R.id.game02Go)
        game3_butt = findViewById(R.id.game03Go)
        cardView2 = findViewById(R.id.cardView11)
        cardView3 = findViewById(R.id.cardView23)

        // Initially disable buttons for game 2 and game 3 and change color
        disableButton(game2_butt, cardView2)
        disableButton(game3_butt, cardView3)

        game1_butt.setOnClickListener {
            goToNextActivity(this, Game1Lev0::class.java)
        }

        game2_butt.setOnClickListener {
            goToNextActivity(this, Game2Lev0::class.java)
        }

        game3_butt.setOnClickListener {
            goToNextActivity(this, Game3Lev1::class.java)
        }

        pieChart1 = findViewById(R.id.pieChart1)
        pieChart2 = findViewById(R.id.pieChart2)
        pieChart3 = findViewById(R.id.pieChart3)

        // Fetch progress data from the server
        fetchProgressData()
    }

    private fun fetchProgressData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2/seniordes/progCalc.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true

                // Prepare POST data
                val userID = (application as MyApp).userID // Get userID from MyApp class
                val postData = "userID=$userID" // Construct POST data
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                // Get response
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()

                    // Parse JSON response as JSONObject
                    val jsonObject = JSONObject(responseData)

                    // Iterate over keys (game IDs) in the JSONObject
                    val keysIterator = jsonObject.keys()
                    while (keysIterator.hasNext()) {
                        val gameID = keysIterator.next().toInt()
                        val progress = jsonObject.getDouble(gameID.toString()).toFloat()

                        println("Game ID: $gameID, Progress: $progress")

                        // Update progress for each game
                        gameProgress[gameID] = progress

                        // Update pie chart based on gameID
                        when (gameID) {
                            1 -> updatePieChart(pieChart1, progress)
                            2 -> updatePieChart(pieChart2, progress)
                            3 -> updatePieChart(pieChart3, progress)
                            // Add more cases if you have more games
                        }
                    }

                    // Enable game 2 button if game 1 progress is 100
                    if (gameProgress[1] == 100f) {
                        runOnUiThread {
                            enableButton(game2_butt, cardView2)
                        }
                    }

                    // Enable game 3 button if game 2 progress is 100
                    if (gameProgress[2] == 100f) {
                        runOnUiThread {
                            enableButton(game3_butt, cardView3)
                        }
                    }
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePieChart(pieChart: PieChart, progress: Float) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(progress, "Completed"))
        entries.add(PieEntry(100 - progress, "Remaining"))

        val dataSet = PieDataSet(entries, "Pie Chart Data")
        dataSet.setColors(Color.parseColor("#A06CD5"), Color.parseColor("#6247AA"))
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setDrawEntryLabels(true)
        pieChart.invalidate()
    }

    // Function to enable a button and change its color
    private fun enableButton(button: Button, cardView: CardView) {
        button.isEnabled = true
        button.backgroundTintList = null

        // Get the original background color of the card view from the XML
        val cardViewBackgroundColor = when (cardView.id) {
            R.id.cardView11 -> getColor(R.color.game2_cardview_color)
            R.id.cardView23 -> getColor(R.color.game3_cardview_color)
            else -> getColor(android.R.color.transparent) // Default to transparent color
        }
        cardView.setCardBackgroundColor(cardViewBackgroundColor)
    }

    // Function to disable a button and change its color
    private fun disableButton(button: Button, cardView: CardView) {
        button.isEnabled = false
        button.setBackgroundColor(Color.parseColor("#E0E0E0")) // Light gray
        cardView.setCardBackgroundColor(Color.parseColor("#CCCCCC")) // Darker gray
    }
}