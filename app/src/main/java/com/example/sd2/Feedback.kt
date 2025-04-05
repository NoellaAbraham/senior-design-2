package com.example.sd2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.AuthFailureError
import com.android.volley.Response

class FeedbackActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var messageField: EditText
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        nameField = findViewById(R.id.feedbackName)
        emailField = findViewById(R.id.feedbackEmail)
        messageField = findViewById(R.id.feedbackMessage)
        submitBtn = findViewById(R.id.submitButton)

        submitBtn.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val message = messageField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                submitFeedback(name, email, message)
            }
        }
    }

    private fun submitFeedback(name: String, email: String, feedback: String) {
        val url = "http://10.0.2.2/seniordes/feedback.php"

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Toast.makeText(this, "Feedback submitted!", Toast.LENGTH_LONG).show()
                nameField.text.clear()
                emailField.text.clear()
                messageField.text.clear()
            },
            Response.ErrorListener {
                Toast.makeText(this, "Submission failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "name" to name,
                    "email" to email,
                    "feedback" to feedback
                )
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mapOf("Content-Type" to "application/x-www-form-urlencoded")
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
