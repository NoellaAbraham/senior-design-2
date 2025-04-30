package com.example.sd2

import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private var userID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cloudLeft = findViewById<ImageView>(R.id.cloudLeft)
        val cloudRight = findViewById<ImageView>(R.id.cloudRight)
        val cloudFloat = AnimationUtils.loadAnimation(this, R.anim.cloud_float)
        cloudLeft.startAnimation(cloudFloat)
        cloudRight.startAnimation(cloudFloat)

        val editTextUsername = findViewById<EditText>(R.id.editTextText)
        val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword)
        val buttonLogin = findViewById<Button>(R.id.button3)
        val buttonReg = findViewById<TextView>(R.id.button4)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                login(username, password)
            } else {
                Toast.makeText(applicationContext, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        buttonReg.setOnClickListener {
            goToNextActivity(this, Registration::class.java)
        }
    }

    private fun login(username: String, password: String) {
        val client = OkHttpClient()
        val url = "http://192.168.0.105/seniordes/login.php"

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginDebug", "Network failure: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("LoginDebug", "Raw response: $body")

                if (response.isSuccessful && body != null && body.trim().startsWith("{")) {
                    try {
                        val json = JSONObject(body)
                        val status = json.optString("status")

                        if (status == "success") {
                            val userID = json.getInt("userID")
                            val userType = json.getString("userType")
                            (application as MyApp).userID = userID

                            runOnUiThread {
                                Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_SHORT).show()
                                when (userType) {
                                    "student" -> goToNextActivity(this@MainActivity, WelcomeActivity::class.java)
                                    "caretaker" -> goToNextActivity(this@MainActivity, CTDashboard::class.java)
                                    "doctor" -> goToNextActivity(this@MainActivity, DoctorDashboardActivity::class.java)
                                    else -> Toast.makeText(applicationContext, "Unknown user type", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginDebug", "JSON parsing error: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Error parsing server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Server response invalid or unexpected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

// === GLOBAL FUNCTIONS ===

fun goToNextActivity(context: Context, nextActivityClass: Class<*>) {
    val intent = Intent(context, nextActivityClass)
    context.startActivity(intent)
}

fun saveProgressToDatabase(userID: Int, gameID: Int, levelID: Int, progress: Int) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val url = URL("http://10.1.38.153/seniordes/progressRep.php")
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.doOutput = true
            urlConnection.requestMethod = "POST"

            val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&progress=$progress"
            urlConnection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                println("Progress saved successfully")
            } else {
                println("Error saving progress")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
