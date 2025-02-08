package com.example.sd2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent

class Chatbot : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // Initialize UI elements
        chatContainer = findViewById(R.id.chatContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        // Handle Send Button Click
        sendButton.setOnClickListener {
            val userMessage = messageInput.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)  // Add User Message
                messageInput.text.clear()

                val botResponse = getBotResponse(userMessage)  // Get Bot Response

                if (botResponse == "NAVIGATE_DOCTORS") {
                    navigateToDoctors()
                } else {
                    addMessage(botResponse, isUser = false)  // Add Bot Response
                }
            }
        }
    }

    // Function to Add Messages (User & Bot)
    private fun addMessage(message: String, isUser: Boolean) {
        val messageTextView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(20, 12, 20, 12)

            // Set text color based on sender
            setTextColor(
                if (isUser) ContextCompat.getColor(context, android.R.color.white)
                else ContextCompat.getColor(context, R.color.pastle_purple)
            )

            // Message bubble styling
            background = if (isUser) {
                ContextCompat.getDrawable(context, R.drawable.user_message_background)
            } else {
                ContextCompat.getDrawable(context, R.drawable.bot_message_background)
            }

            // Align messages
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 10, 10, 10)
                gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
            }
            layoutParams = params
        }

        chatContainer.addView(messageTextView)  // Add message to chat window
    }


    private fun navigateToDoctors() {
        val intent = Intent(this@Chatbot, DoctorsAppointment::class.java)
        startActivity(intent)
    }

    // Predefined Bot Responses
    private fun getBotResponse(userMessage: String): String {
        val responses = mapOf(
            "hello" to "Hi there! How can I assist you today?",
            "how are you" to "I'm just a bot, but I'm here to help!",
            "what is autism" to "Autism is a developmental disorder affecting communication and behavior.",
            "thank you" to "You're welcome!",
            "bye" to "Goodbye! Have a great day!",
            "i want to book an appointment" to "NAVIGATE_DOCTORS"  // Triggers navigation
        )

        return responses[userMessage.lowercase()] ?: "I'm not sure how to respond to that. Try asking something else!"
    }
}
