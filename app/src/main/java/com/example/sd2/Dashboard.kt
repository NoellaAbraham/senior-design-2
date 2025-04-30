package com.example.sd2
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
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

class Dashboard : FragmentActivity() {

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

        disableButton(game2_butt, cardView2)
        disableButton(game3_butt, cardView3)

        game1_butt.setOnClickListener {
            showFocusPopup(Game1Lev0::class.java)
        }

        game2_butt.setOnClickListener {
            showFocusPopup(Game2Lev0::class.java)
        }

        game3_butt.setOnClickListener {
            showFocusPopup(Game3Lev1::class.java)
        }

        pieChart1 = findViewById(R.id.pieChart1)
        pieChart2 = findViewById(R.id.pieChart2)
        pieChart3 = findViewById(R.id.pieChart3)

        fetchProgressData()
    }

    private fun showFocusPopup(nextActivity: Class<*>) {
        val popup = FocusPopupDialogFragment.newInstance(nextActivity)
        popup.show(supportFragmentManager, "FocusPopup")
    }

    private fun fetchProgressData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.0.105/seniordes/progCalc.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true

                val userID = (application as MyApp).userID
                val postData = "userID=$userID"
                urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseData = urlConnection.inputStream.bufferedReader().readText()
                    val jsonObject = JSONObject(responseData)
                    val keysIterator = jsonObject.keys()

                    while (keysIterator.hasNext()) {
                        val gameID = keysIterator.next().toInt()
                        val progress = jsonObject.getDouble(gameID.toString()).toFloat()
                        gameProgress[gameID] = progress

                        when (gameID) {
                            1 -> updatePieChart(pieChart1, progress)
                            2 -> updatePieChart(pieChart2, progress)
                            3 -> updatePieChart(pieChart3, progress)
                        }
                    }

                    if (gameProgress[1] == 100f) {
                        runOnUiThread { enableButton(game2_butt, cardView2) }
                    }

                    if (gameProgress[2] == 100f) {
                        runOnUiThread { enableButton(game3_butt, cardView3) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePieChart(pieChart: PieChart, progress: Float) {
        val entries = arrayListOf(
            PieEntry(progress, "Completed"),
            PieEntry(100 - progress, "Remaining")
        )

        val dataSet = PieDataSet(entries, "Progress")
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

    private fun enableButton(button: Button, cardView: CardView) {
        button.isEnabled = true
        button.backgroundTintList = null
        val cardViewBackgroundColor = when (cardView.id) {
            R.id.cardView11 -> getColor(R.color.game2_cardview_color)
            R.id.cardView23 -> getColor(R.color.game3_cardview_color)
            else -> getColor(android.R.color.transparent)
        }
        cardView.setCardBackgroundColor(cardViewBackgroundColor)
    }

    private fun disableButton(button: Button, cardView: CardView) {
        button.isEnabled = false
        button.setBackgroundColor(Color.parseColor("#E0E0E0"))
        cardView.setCardBackgroundColor(Color.parseColor("#CCCCCC"))
    }
}
