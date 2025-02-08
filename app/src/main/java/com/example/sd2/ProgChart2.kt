package com.example.sd2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView

class ProgChart2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prog_chart2)


        val imageButton7 = findViewById<ImageButton>(R.id.home_button)

        imageButton7.setOnClickListener {
            val intent = Intent(this, CTDash2::class.java)
            startActivity(intent)
        }


        // Sample data
        val studentName = "Maya Jones"
        val scoresGame1 = listOf(
            Entry(1f, 30f), // Game 1 Day 1
            Entry(2f, 61f), // Game 1 Day 2
            Entry(3f, 89f)  // Game 1 Day 3
        )

        val scoresGame2 = listOf(
            Entry(1f, 50f), // Game 2 Day 1
            Entry(2f, 100f), // Game 2 Day 2
            Entry(3f, 100f)  // Game 2 Day 3
        )

        val scoresGame3 = listOf(
            Entry(1f, 87f), // Game 3 Day 1
            Entry(2f, 91f), // Game 3 Day 2
            Entry(3f, 95f)  // Game 3 Day 3
        )

        // Set student name
        findViewById<TextView>(R.id.studentName).text = studentName

        // Create and configure the line chart
        val lineChart = LineChart(this)

        // LineDataSet for Game 1
        val lineDataSetGame1 = LineDataSet(scoresGame1, "Game 1 Scores")
        lineDataSetGame1.color = Color.BLUE
        lineDataSetGame1.valueTextColor = Color.BLACK
        lineDataSetGame1.valueTextSize = 16f

        // LineDataSet for Game 2
        val lineDataSetGame2 = LineDataSet(scoresGame2, "Game 2 Scores")
        lineDataSetGame2.color = Color.RED
        lineDataSetGame2.valueTextColor = Color.BLACK
        lineDataSetGame2.valueTextSize = 16f

        // LineDataSet for Game 3
        val lineDataSetGame3 = LineDataSet(scoresGame3, "Game 3 Scores")
        lineDataSetGame3.color = Color.GREEN
        lineDataSetGame3.valueTextColor = Color.BLACK
        lineDataSetGame3.valueTextSize = 16f

        val lineData = LineData(lineDataSetGame1, lineDataSetGame2, lineDataSetGame3)
        lineChart.data = lineData

        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value) {
                    1f -> " Day 1 "
                    2f -> " Day 2 "
                    3f -> " Day 3 "
                    else -> value.toString()
                }
            }
        })
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        // Add the line chart to the CardView
        val chartContainer = findViewById<FrameLayout>(R.id.chart_container)
        chartContainer.addView(lineChart)
    }
}
