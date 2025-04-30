package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class Chatbot : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuIcon: ImageView

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button

    private val client = OkHttpClient()
    private val apiKey = " " //openAI API KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // Drawer setup
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        menuIcon = findViewById(R.id.menuIcon)

        menuIcon.setOnClickListener {
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

        // Chat UI
        chatContainer = findViewById(R.id.chatContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            val userMessage = messageInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)
                messageInput.text.clear()
                getBotResponse(userMessage)
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val messageTextView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(20, 12, 20, 12)
            setTextColor(
                if (isUser) ContextCompat.getColor(context, android.R.color.white)
                else ContextCompat.getColor(context, R.color.dark_blue)
            )
            background = if (isUser)
                ContextCompat.getDrawable(context, R.drawable.user_message_background)
            else
                ContextCompat.getDrawable(context, R.drawable.bot_message_background)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 10, 10, 10)
                gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
            }
        }

        chatContainer.addView(messageTextView)
    }

    private fun getBotResponse(userMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaType = "application/json".toMediaType()
                val requestBody = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().put(
                        JSONObject().put("role", "user").put("content", userMessage)
                    ))
                }

                val body = requestBody.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val botReply = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    runOnUiThread {
                        handleKeywordNavigation(userMessage.lowercase())
                        addMessage(botReply.trim(), isUser = false)
                    }
                } else {
                    runOnUiThread {
                        addMessage("Oops, I couldn't get a response. Try again!", isUser = false)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    addMessage("Error: ${e.message}", isUser = false)
                }
            }
        }
    }

    private fun handleKeywordNavigation(message: String) {
        when {
            message.contains("resource") || message.contains("learn") -> {
                startActivity(Intent(this, ResourcesActivity::class.java))
            }
            message.contains("feedback") || message.contains("report") -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            message.contains("doctor") || message.contains("appointment") || message.contains("book") -> {
                startActivity(Intent(this, DoctorsAppointment::class.java))
            }
        }
    }
}
