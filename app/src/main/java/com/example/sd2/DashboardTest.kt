package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import android.view.View

class DashboardTest : AppCompatActivity() {

    private lateinit var gameContainer: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_test)

        gameContainer = findViewById(R.id.dashboardContainer)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)

        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem: MenuItem ->
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

        val userID = (application as MyApp).userID
        fetchUserScores(userID)
    }

    private fun fetchUserScores(userID: Int) {
        val url = "http://192.168.0.105/seniordes/get_scores.php?userID=$userID"

        val request = object : StringRequest(Method.GET, url,
            Response.Listener { response ->
                val jsonArray = JSONArray(response)
                val latestAttempts = mutableMapOf<Pair<Int, Int>, LevelResult>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val gameId = obj.getInt("gameID")
                    val levelId = obj.getInt("levelID")
                    val mistakes = obj.getInt("mistakes")
                    val timeStr = obj.getString("time")
                    val timeParts = timeStr.split(":")
                    val timeInSeconds = if (timeStr.contains(":")) {
                        val timeParts = timeStr.split(":")
                        timeParts[0].toInt() * 60 + timeParts[1].toInt()
                    } else {
                        timeStr.toIntOrNull() ?: 0
                    }


                    val key = Pair(gameId, levelId)
                    latestAttempts[key] = LevelResult(timeInSeconds, mistakes, levelId)
                }

                val groupedGames = mutableMapOf<String, MutableList<LevelResult>>()

                latestAttempts.forEach { (key, result) ->
                    val gameId = key.first
                    val levelId = key.second

                    val gameTitle = when {
                        gameId == 2 && levelId in 7..10 -> "Game 2 - Emotion Recognition"
                        gameId == 2 && levelId in 31..32 -> "Game 2 - Situational Understanding"
                        gameId == 3 && levelId == 15 -> "Game 3 - Match The Emotion"
                        gameId == 4 && levelId in 41..46 -> "Game 4 - AI Emotion Detection"

                        else -> "Game $gameId"
                    }

                    groupedGames.getOrPut(gameTitle) { mutableListOf() }.add(result)
                }

                for ((gameTitle, levels) in groupedGames) {
                    when (gameTitle) {
                        "Game 2 - Emotion Recognition" -> addGame2StatsAsPie(gameTitle, levels)
                        "Game 2 - Situational Understanding" -> addSituationalStatsAsPie(gameTitle, levels)
                        "Game 3 - Match The Emotion" -> addGame3BarGraph(gameTitle, levels)
                        "Game 4 - AI Emotion Detection" -> addGame4PieChart(gameTitle, levels)
                        else -> addGameStatsToDashboard(GameStats(gameTitle, levels))
                    }
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Failed to load scores", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            }
        ) {}

        Volley.newRequestQueue(this).add(request)
    }

    private fun addGameStatsToDashboard(gameStats: GameStats) {
        val chartLayout = layoutInflater.inflate(R.layout.dashboard_game_stats, gameContainer, false)

        val title = chartLayout.findViewById<TextView>(R.id.gameTitle)
        val barChart = chartLayout.findViewById<BarChart>(R.id.barChart)
        val scoreText = chartLayout.findViewById<TextView>(R.id.scoreText)
        val feedbackText = chartLayout.findViewById<TextView>(R.id.feedbackText)
        val pieChart = chartLayout.findViewById<PieChart>(R.id.pieChart)

        pieChart.visibility = View.GONE

        title.text = gameStats.title

        val barEntries = ArrayList<BarEntry>()
        val xLabels = ArrayList<String>()

        gameStats.levels.forEachIndexed { index, result ->
            barEntries.add(BarEntry(index.toFloat(), result.timeTaken.toFloat()))
            xLabels.add("L${result.levelId}")
        }

        val barDataSet = BarDataSet(barEntries, "Time vs Tries")
        barDataSet.color = ColorTemplate.MATERIAL_COLORS[0]
        val barData = BarData(barDataSet)
        barData.barWidth = 0.4f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.description = Description().apply { text = "Time (Y) vs Tries (X)" }

        if (barData.entryCount == 0) {
            barChart.setNoDataText("No data available")
        } else {
            barChart.setNoDataText("")
        }

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.axisMinimum = 0f
        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        barChart.axisRight.isEnabled = false

        barChart.invalidate()

        val totalTime = gameStats.levels.sumOf { it.timeTaken }
        val totalTries = gameStats.levels.sumOf { it.tries }

        val score = (100 - (totalTries.toFloat() / 10)).toInt().coerceAtLeast(0)

        scoreText.text = "Score: $score/100"
        feedbackText.text = getFeedback(score, totalTime, totalTries)

        gameContainer.addView(chartLayout)
    }

    private fun addGame2StatsAsPie(title: String, levels: List<LevelResult>) {
        val chartLayout = layoutInflater.inflate(R.layout.dashboard_game_stats, gameContainer, false)

        chartLayout.findViewById<TextView>(R.id.gameTitle).text = title
        chartLayout.findViewById<BarChart>(R.id.barChart).visibility = View.GONE
        val pieChart = chartLayout.findViewById<PieChart>(R.id.pieChart)
        val scoreText = chartLayout.findViewById<TextView>(R.id.scoreText)
        val feedbackText = chartLayout.findViewById<TextView>(R.id.feedbackText)

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        var totalScore = 0

        levels.forEach { level ->
            val score = (100 - (level.tries * 20)).coerceAtLeast(0)

            totalScore += score
            val label = when (level.levelId) {
                7 -> "Happy"
                8 -> "Angry"
                9 -> "Sad"
                10 -> "Fear"
                else -> "Level ${level.levelId}"
            }
            entries.add(PieEntry(score.toFloat(), label))
            colors.add(ColorTemplate.MATERIAL_COLORS[(level.levelId - 7) % ColorTemplate.MATERIAL_COLORS.size])
        }

        val dataSet = PieDataSet(entries, "Emotion Scores")
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false

        if (pieData.entryCount == 0) {
            pieChart.setNoDataText("No data available")
        } else {
            pieChart.setNoDataText("")
        }

        pieChart.invalidate()

        val averageScore = if (levels.isNotEmpty()) totalScore / levels.size else 0
        scoreText.text = "Average Score: $averageScore/100"
        feedbackText.text = getFeedback(averageScore, 0, levels.sumOf { it.tries })

        gameContainer.addView(chartLayout)
    }

    private fun addSituationalStatsAsPie(title: String, levels: List<LevelResult>) {
        val chartLayout = layoutInflater.inflate(R.layout.dashboard_game_stats, gameContainer, false)

        chartLayout.findViewById<TextView>(R.id.gameTitle).text = title
        chartLayout.findViewById<BarChart>(R.id.barChart).visibility = View.GONE
        val pieChart = chartLayout.findViewById<PieChart>(R.id.pieChart)
        val scoreText = chartLayout.findViewById<TextView>(R.id.scoreText)
        val feedbackText = chartLayout.findViewById<TextView>(R.id.feedbackText)

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        var totalScore = 0

        levels.forEach { level ->
            val score = (100 - (level.tries * 20)).coerceAtLeast(0)
            totalScore += score

            val label = when (level.levelId) {
                31 -> "Scene 1"
                32 -> "Scene 2"
                else -> "Level ${level.levelId}"
            }

            entries.add(PieEntry(score.toFloat(), label))
            colors.add(ColorTemplate.MATERIAL_COLORS[level.levelId % ColorTemplate.MATERIAL_COLORS.size])
        }

        val dataSet = PieDataSet(entries, "Situational Understanding")
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false

        if (pieData.entryCount == 0) {
            pieChart.setNoDataText("No data available")
        } else {
            pieChart.setNoDataText("")
        }

        pieChart.invalidate()

        val averageScore = if (levels.isNotEmpty()) totalScore / levels.size else 0
        scoreText.text = "Average Score: $averageScore/100"
        feedbackText.text = getFeedback(averageScore, 0, levels.sumOf { it.tries })

        gameContainer.addView(chartLayout)
    }

    private fun addGame3BarGraph(title: String, levels: List<LevelResult>) {
        val chartLayout = layoutInflater.inflate(R.layout.dashboard_game_stats, gameContainer, false)

        val titleView = chartLayout.findViewById<TextView>(R.id.gameTitle)
        val barChart = chartLayout.findViewById<BarChart>(R.id.barChart)
        val pieChart = chartLayout.findViewById<PieChart>(R.id.pieChart)
        val scoreText = chartLayout.findViewById<TextView>(R.id.scoreText)
        val feedbackText = chartLayout.findViewById<TextView>(R.id.feedbackText)

        pieChart.visibility = View.GONE
        titleView.text = title

        val xLabels = mutableListOf<String>()
        val timeEntries = mutableListOf<BarEntry>()
        val scoreEntries = mutableListOf<BarEntry>()

        levels.forEachIndexed { index, result ->
            xLabels.add("L${result.levelId}")
            timeEntries.add(BarEntry(index.toFloat(), result.timeTaken.toFloat()))
            val score = (100 - result.tries * 10).coerceAtLeast(0)
            scoreEntries.add(BarEntry(index.toFloat() + 0.4f, score.toFloat()))
        }

        val timeSet = BarDataSet(timeEntries, "Time (s)")
        timeSet.color = ColorTemplate.MATERIAL_COLORS[0]

        val scoreSet = BarDataSet(scoreEntries, "Score")
        scoreSet.color = ColorTemplate.MATERIAL_COLORS[1]

        val data = BarData(timeSet, scoreSet)
        data.barWidth = 0.4f
        barChart.data = data

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            axisMinimum = 0f
            valueFormatter = IndexAxisValueFormatter(xLabels)
        }

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description = Description().apply { text = "Time vs Score" }
        barChart.groupBars(0f, 0.2f, 0.05f) // group bars: (fromX, groupSpace, barSpace)
        barChart.invalidate()

        val totalScore = scoreEntries.sumOf { it.y.toInt() }
        val averageScore = if (levels.isNotEmpty()) totalScore / levels.size else 0
        scoreText.text = "Avg. Score: $averageScore/100"
        feedbackText.text = getFeedback(averageScore, 0, levels.sumOf { it.tries })

        gameContainer.addView(chartLayout)
    }

    private fun addGame4PieChart(title: String, levels: List<LevelResult>) {
        val chartLayout = layoutInflater.inflate(R.layout.dashboard_game_stats, gameContainer, false)

        chartLayout.findViewById<TextView>(R.id.gameTitle).text = title
        chartLayout.findViewById<BarChart>(R.id.barChart).visibility = View.GONE
        val pieChart = chartLayout.findViewById<PieChart>(R.id.pieChart)
        val scoreText = chartLayout.findViewById<TextView>(R.id.scoreText)
        val feedbackText = chartLayout.findViewById<TextView>(R.id.feedbackText)

        val emotions = listOf("Happy", "Sad", "Neutral", "Angry", "Surprised", "Fear")
        val levelIDs = listOf(41, 42, 43, 44, 45, 46) // Game 4 levelIDs

        val entries = ArrayList<PieEntry>()
        val colors = ColorTemplate.MATERIAL_COLORS.toMutableList()

        levelIDs.forEachIndexed { index, levelID ->
            val level = levels.find { it.levelId == levelID }
            if (level != null) {
                entries.add(PieEntry(level.timeTaken.toFloat(), emotions[index]))
            }
        }

        val dataSet = PieDataSet(entries, "Reaction Time per Emotion")
        dataSet.colors = colors
        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.setUsePercentValues(false) //show seconds not %
        pieChart.description.isEnabled = false
        pieChart.invalidate()

        scoreText.text = "Lower time is better!"
        feedbackText.text = "This shows how quickly each emotion was detected."

        gameContainer.addView(chartLayout)
    }




    private fun getFeedback(score: Int, time: Int, tries: Int): String {
        return when {
            score >= 95 -> "You're amazing! Excellent performance!"
            score >= 85 -> "Great work! Keep it up!"
            score >= 70 -> "Good effort! You did your best and that's awesome!"
            else -> "You're learning at your own pace — proud of you!"
        }
    }

    data class GameStats(val title: String, val levels: List<LevelResult>)
    data class LevelResult(val timeTaken: Int, val tries: Int, val levelId: Int)
}