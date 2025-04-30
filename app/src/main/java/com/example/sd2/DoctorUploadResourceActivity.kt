package com.example.sd2

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class DoctorUploadResourceActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var linkInput: EditText
    private lateinit var uploadBtn: Button

    private val uploadUrl = "http://192.168.0.105/seniordes/upload_resource.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_upload_resource)

        titleInput = findViewById(R.id.resourceTitle)
        descriptionInput = findViewById(R.id.resourceDescription)
        linkInput = findViewById(R.id.resourceLink) // <-- Add this EditText in XML!
        uploadBtn = findViewById(R.id.uploadButton)

        uploadBtn.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val link = linkInput.text.toString().trim()

            if (title.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "Title and Link are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadResource(title, description, link)
        }
    }

    private fun uploadResource(title: String, description: String, link: String) {
        val formBody = FormBody.Builder()
            .add("doctorID", "1") // or retrieve dynamically
            .add("title", title)
            .add("description", description)
            .add("link", link)
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(formBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DoctorUploadResourceActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DoctorUploadResourceActivity, "Resource uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@DoctorUploadResourceActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
