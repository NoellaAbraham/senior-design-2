package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class Chatbot : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button

    private val client = OkHttpClient()
    private val apiKey = "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" // replace with your real key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

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
                else ContextCompat.getColor(context, R.color.pastle_purple)
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
                        // check for keywords
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
