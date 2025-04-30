package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ResourcesActivity : AppCompatActivity() {

    private lateinit var resourceContainer: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private val resourcesUrl = "http://192.168.0.105/seniordes/get_resources.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)

        // Find views
        resourceContainer = findViewById(R.id.resourceContainer)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)

        // Open drawer on icon click
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle navigation item clicks
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

        // Fetch and show resources
        fetchResources()
    }

    private fun fetchResources() {
        val request = Request.Builder()
            .url(resourcesUrl)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ResourcesActivity, "Failed to load resources", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonObject = JSONObject(responseData)
                        val success = jsonObject.getBoolean("success")
                        if (success) {
                            val resourcesArray = jsonObject.getJSONArray("resources")
                            runOnUiThread {
                                for (i in 0 until resourcesArray.length()) {
                                    val resource = resourcesArray.getJSONObject(i)
                                    val title = resource.getString("title")
                                    val description = resource.getString("description")
                                    val link = resource.getString("link")
                                    addResource(title, description, link)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun addResource(title: String, description: String, link: String) {
        val resourceView = TextView(this).apply {
            text = "📘 $title\n$description"
            textSize = 16f
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
            setBackgroundResource(R.drawable.blur_fake_background)
            setOnClickListener {
                openResourcePopup(link)
            }
        }

        resourceContainer.addView(resourceView)
    }

    private fun openResourcePopup(link: String) {
        val dialog = ResourcePopupDialogFragment.newInstance(link)
        dialog.show(supportFragmentManager, "ResourcePopup")
    }
}
