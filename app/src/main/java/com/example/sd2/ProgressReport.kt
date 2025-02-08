package com.example.sd2

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class EmotionAverages(
    val avg_angry: Double,
    val avg_disgust: Double,
    val avg_fear: Double,
    val avg_happy: Double,
    val avg_neutral: Double,
    val avg_sad: Double,
    val avg_surprise: Double
)

/*interface ApiService {
    @GET("emotionGet.php")
    fun getEmotionAverages(@Query("userID") userID: Int): Call<EmotionAverages>
}*/



class ProgressReport : AppCompatActivity() {

    private lateinit var game1DisableButton: Button
    private lateinit var game2DisableButton: Button
    private lateinit var game3DisableButton: Button

    private lateinit var barChart: BarChart

    private val gameStatusViewModel: GameStatusViewModel by viewModels()
    private fun getUserIdFromIntent(): Int {
        return intent.getIntExtra("USER_ID", -1) // -1 is the default value if USER_ID is not found
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_report)

        val imageButton7 = findViewById<ImageButton>(R.id.home_button)

        barChart = findViewById(R.id.barChart)

        imageButton7.setOnClickListener {
            val intent = Intent(this, CTDashboard::class.java)
            startActivity(intent)
        }

        val userId = getUserIdFromIntent()
        if (userId != -1) {
            // Fetch progress data for the specific user
            fetchProgressData(userId)
            fetchEmotionAverages(userId) // this is called to fetch and display emotion averages
            fetchCorrectAnswers(userId)
        } else {
            Log.e("ProgressReport", "User ID not found in intent extras")
            showToast("User ID not found")
            finish()
        }

        game1DisableButton = findViewById(R.id.disableGame01)
        game2DisableButton = findViewById(R.id.disableGame02)
        game3DisableButton = findViewById(R.id.disableGame03)

        game1DisableButton.setOnClickListener {
            gameStatusViewModel.game1Enabled.value = false
            // Update UI to reflect the disabled state
        }

        game2DisableButton.setOnClickListener {
            gameStatusViewModel.game2Enabled.value = false
            // Update UI to reflect the disabled state
        }

        game3DisableButton.setOnClickListener {
            gameStatusViewModel.game3Enabled.value = false
            // Update UI to reflect the disabled state
        }
    }

    private fun fetchProgressData(userId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.56.1/seniordes/progCalc.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true

                // Prepare POST data
                val postData = "userID=$userId" // Construct POST data
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                // Get response
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()

                    // Check if response is valid JSON
                    if (responseData.isNotEmpty() && responseData.startsWith("{")) {
                        // Response is JSON object
                        val jsonObject = JSONObject(responseData)

                        if (jsonObject.has("error")) {
                            // User not found in the table, handle this case
                            Log.e("ProgressReport", "User not found in the table")
                            showToast("User not found")
                            runOnUiThread {
                                changeCardViewsToGray()
                            }
                        } else {
                            // Iterate over game IDs and update card views accordingly
                            val gameIds = listOf(1, 2, 3) // Assuming game IDs 1, 2, 3
                            runOnUiThread {
                                gameIds.forEach { gameId ->
                                    val progress =
                                        jsonObject.optDouble(gameId.toString(), 0.0).toFloat()
                                    updateCardView(getCardViewId(gameId), progress)
                                }
                            }
                            // Fetch additional data (total wrong answers and total time taken)
                            fetchAdditionalData(userId)
                        }
                    } else {
                        // Response is not a JSON object, handle this case
                        Log.e("ProgressReport", "Invalid response format")
                    }
                } else {
                    // HTTP error, handle this case
                    Log.e("ProgressReport", "HTTP error code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ProgressReport", "Error fetching progress data: ${e.message}")
            }
        }
    }

    private fun fetchAdditionalData(userId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.56.1/seniordes/progRepGet.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true

                // Prepare POST data
                val postData = "userID=$userId" // Construct POST data
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                // Get response
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()

                    // Check if response is valid JSON
                    if (responseData.isNotEmpty() && responseData.startsWith("{")) {
                        // Response is JSON object
                        val jsonObject = JSONObject(responseData)

                        if (jsonObject.has("error")) {
                            // User not found in the table, handle this case
                            Log.e("ProgressReport", "User not found in the table")
                            showToast("User not found")
                        } else {
                            // Iterate over game IDs and update text views accordingly
                            val gameIds = listOf(1, 2, 3) // Assuming game IDs 1, 2, 3
                            runOnUiThread {
                                gameIds.forEach { gameId ->
                                    val gameData = jsonObject.optJSONObject("$gameId")
                                    if (gameData != null) {
                                        val average = gameData.optInt("average", 0)
                                        val totalTimeTaken = gameData.optString("totalTimeTaken", "00:00:00")
                                        val formattedTime = formatTime(totalTimeTaken)
                                        updateTextViews(gameId, average, formattedTime)
                                    }

                                }
                            }
                        }
                    } else {
                        // Response is not a JSON object, handle this case
                        Log.e("ProgressReport", "Invalid response format")
                    }
                } else {
                    // HTTP error, handle this case
                    Log.e("ProgressReport", "HTTP error code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ProgressReport", "Error fetching additional data: ${e.message}")
            }
        }
    }

    private fun fetchCorrectAnswers(userId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.56.1/seniordes/updateCorrectAnswers.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"

                val postData = "userID=$userId"
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()

                    if (responseData.isNotEmpty()) {
                        val jsonArray = JSONArray(responseData)
                        val dataEntries = mutableListOf<Entry>()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val gameID = jsonObject.getInt("gameID").toFloat()
                            val correctAnswers = jsonObject.getInt("correctAnswers").toFloat()
                            dataEntries.add(Entry(gameID, correctAnswers))
                        }

                        runOnUiThread {
                            displayLineChart(dataEntries)
                        }
                    } else {
                        Log.e("ProgressReport", "Empty response")
                    }
                } else {
                    Log.e("ProgressReport", "HTTP error code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ProgressReport", "Error fetching CorrectAnswers data: ${e.message}")
            }
        }
    }

    private fun displayLineChart(dataEntries: List<Entry>) {
        val lineChart = findViewById<LineChart>(R.id.lineChart)
        val lineDataSet = LineDataSet(dataEntries, "Correct Answers")
        lineDataSet.color = Color.BLUE
        lineDataSet.valueTextColor = Color.BLACK
        lineDataSet.valueTextSize = 12f
        lineDataSet.setDrawCircles(true)
        lineDataSet.circleColors = listOf(Color.RED)

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.isEnabled = false
        lineChart.invalidate() // Refresh the chart
    }








    //  private fun calculateAverage(gameId: Int, leastWrongAnswers: Int): String {
    //    val totalQuestions = when (gameId) {
      //      1 -> 12
      //      2 -> 12 // Assuming levelID 1 and 2 have 12 questions each
      //      3 -> 2  // LevelID 3 has 2 questions
      //      else -> 0
      //  }

        //return if (totalQuestions > 0) {
        //    val CorrectAnswers = totalQuestions - leastWrongAnswers
        //    "$CorrectAnswers/$totalQuestions"
       // } else {
        //    "0/0"
       // }
   // }


    private fun formatTime(time: String?): String {
        if (time.isNullOrEmpty()) return "00:00" // Return default if time is null or empty
        val parts = time.split(":")
        if (parts.size >= 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            return String.format("%02d:%02d", minutes, seconds)
        }
        return "00:00" // Return default if time format is invalid
    }

    private fun updateTextViews(gameId: Int, average: Int, timeTaken: String) {
        val textViewAverage = findViewById<TextView>(getTextViewId(gameId, "average"))
        val textViewTimeTaken = findViewById<TextView>(getTextViewId(gameId, "timeTaken"))

        textViewAverage.text = "$average"
        textViewTimeTaken.text = "$timeTaken"
    }

    private fun getTextViewId(gameId: Int, dataType: String): Int {
        return when (gameId) {
            1 -> when (dataType) {
                "average" -> R.id.game01CorrectAnswers
                "timeTaken" -> R.id.game01TimeTaken
                else -> throw IllegalArgumentException("Invalid data type")
            }
            2 -> when (dataType) {
                "average" -> R.id.game02CorrectAnswers
                "timeTaken" -> R.id.game02TimeTaken
                else -> throw IllegalArgumentException("Invalid data type")
            }
            3 -> when (dataType) {
                "average" -> R.id.game03CorrectAnswers
                "timeTaken" -> R.id.game03TimeTaken
                else -> throw IllegalArgumentException("Invalid data type")
            }
            else -> throw IllegalArgumentException("Invalid game ID")
        }
    }

    private fun getCardViewId(gameId: Int): Int {
        return when (gameId) {
            1 -> R.id.game01
            2 -> R.id.game02
            3 -> R.id.game03
            else -> throw IllegalArgumentException("Invalid game ID")
        }
    }

    private fun updateCardView(gameId: Int, progress: Float) {
        val cardView = findViewById<CardView>(gameId)
        val grayColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        if (progress < 100f) {
            runOnUiThread {
                cardView.setCardBackgroundColor(grayColor)
            }
        }
    }

    private fun changeCardViewsToGray() {
        val grayColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        val gameIds = listOf(R.id.game01, R.id.game02, R.id.game03) // Assuming game IDs 1, 2, 3
        runOnUiThread {
            gameIds.forEach { gameId ->
                findViewById<CardView>(gameId)?.setCardBackgroundColor(grayColor)
            }
        }
    }

    private fun fetchEmotionAverages(userId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.56.1/seniordes/emotionGet.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true

                // Prepare POST data
                val postData = "userID=$userId" // Construct POST data
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                // Get response
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()

                    // Check if response is valid JSON
                    if (responseData.isNotEmpty() && responseData.startsWith("{")) {
                        // Response is JSON object
                        val jsonObject = JSONObject(responseData)

                        if (jsonObject.has("error")) {
                            // User not found in the table, handle this case
                            Log.e("ProgressReport", "User not found in the table")
                            showToast("User not found")
                            runOnUiThread {
                                changeCardViewsToGray()
                            }
                        } else {
                            // Assuming the successful response contains the emotion averages data
                            val emotionAverages = parseEmotionAverages(jsonObject)
                            runOnUiThread {
                                displayBarChart(emotionAverages)
                            }
                        }
                    } else {
                        // Response is not a JSON object, handle this case
                        Log.e("ProgressReport", "Invalid response format")
                    }
                } else {
                    // HTTP error, handle this case
                    Log.e("ProgressReport", "HTTP error code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ProgressReport", "Error fetching progress data: ${e.message}")
            }
        }
    }

    private fun parseEmotionAverages(jsonObject: JSONObject): EmotionAverages {
        // Assuming the JSON object has the structure to parse into EmotionAverages
        // Example parsing logic, update based on the actual JSON structure
        val averageAngry = jsonObject.getDouble("avg_angry")
        val averageDisgust = jsonObject.getDouble("avg_disgust")
        val averageFear = jsonObject.getDouble("avg_fear")
        val averageHappiness = jsonObject.getDouble("avg_happy")
        val averageNeutral = jsonObject.getDouble("avg_neutral")
        val averageSadness = jsonObject.getDouble("avg_sad")
        val averageSurprise = jsonObject.getDouble("avg_surprise")

        // Add other emotions as needed
        return EmotionAverages(averageAngry, averageDisgust, averageFear, averageHappiness, averageNeutral, averageSadness, averageSurprise)
    }
        /* val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.56.1/seniordes/emotionGet.php") // replace with your server address
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getEmotionAverages(userId).enqueue(object : Callback<EmotionAverages> {
            override fun onResponse(call: Call<EmotionAverages>, response: Response<EmotionAverages>) {
                if (response.isSuccessful) {
                    val emotionAverages = response.body() ?: return
                    displayBarChart(emotionAverages)
                } else {
                    Log.e("ProgressReport", "Response not successful")
                }
            }

            override fun onFailure(call: Call<EmotionAverages>, t: Throwable) {
                Log.e("ProgressReport", "Failed to fetch emotion averages: ${t.message}")
            }
        })
    }*/

    private fun displayBarChart(emotionAverages: EmotionAverages) {
        val entries = listOf(
            BarEntry(0f, emotionAverages.avg_angry.toFloat()),
            BarEntry(1f, emotionAverages.avg_disgust.toFloat()),
            BarEntry(2f, emotionAverages.avg_fear.toFloat()),
            BarEntry(3f, emotionAverages.avg_happy.toFloat()),
            BarEntry(4f, emotionAverages.avg_neutral.toFloat()),
            BarEntry(5f, emotionAverages.avg_sad.toFloat()),
            BarEntry(6f, emotionAverages.avg_surprise.toFloat())
        )

        val dataSet = BarDataSet(entries, "Emotion Averages").apply {
            colors = listOf(
                Color.RED,         // Angry
                Color.GREEN,       // Disgust
                Color.MAGENTA,     // Fear
                Color.YELLOW,      // Happy
                Color.LTGRAY,      // Neutral
                Color.BLUE,        // Sad
                Color.CYAN         // Surprised
            )
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            valueTypeface = Typeface.DEFAULT_BOLD
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // Set the background color to white
        barChart.setBackgroundColor(Color.WHITE)

        // Disable grid background and lines
        barChart.setDrawGridBackground(true)
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisRight.setDrawGridLines(true)
        barChart.xAxis.setDrawGridLines(true)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise")
        )
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 12f
        xAxis.typeface = Typeface.DEFAULT_BOLD

        val yAxisLeft = barChart.axisLeft
        yAxisLeft.granularity = 1f
        yAxisLeft.axisMaximum = 100f // Set maximum y-axis value to 100
        val yAxisRight = barChart.axisRight
        yAxisRight.granularity = 1f
        yAxisRight.axisMaximum = 100f // Set maximum y-axis value to 100

        barChart.description.isEnabled = false
        barChart.invalidate()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
