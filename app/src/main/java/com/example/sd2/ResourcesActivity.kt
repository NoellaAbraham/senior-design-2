package com.example.sd2

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResourcesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)  // This links to the XML layout

        // Handle "Learn More" clicks
        val learnMore1 = findViewById<TextView>(R.id.learnMore1)
        val learnMore2 = findViewById<TextView>(R.id.learnMore2)
        val learnMore3 = findViewById<TextView>(R.id.learnMore3)
        val learnMore4 = findViewById<TextView>(R.id.learnMore4)



        learnMore1.setOnClickListener {
            Toast.makeText(this, "Learn more about Autism Spectrum Disorder 1", Toast.LENGTH_SHORT).show()
        }

        learnMore2.setOnClickListener {
            Toast.makeText(this, "Learn more about Autism Spectrum Disorder 2", Toast.LENGTH_SHORT).show()
        }

        learnMore3.setOnClickListener {
            Toast.makeText(this, "Learn more about Autism Spectrum Disorder 3", Toast.LENGTH_SHORT).show()
        }

        learnMore4.setOnClickListener {
            Toast.makeText(this, "Learn more about Autism Spectrum Disorder 3", Toast.LENGTH_SHORT).show()
        }
    }
}
