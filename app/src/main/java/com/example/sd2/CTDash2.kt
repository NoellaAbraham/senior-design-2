package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CTDash2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ctdash2)

        // Set up the window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listeners for buttons
        findViewById<Button>(R.id.goToProfile1).setOnClickListener {
            navigateToProgChart1()
        }

        findViewById<Button>(R.id.goToProfile2).setOnClickListener {
            navigateToProgChart2()
        }
    }

    // Function to handle navigation to ProgChart1
    private fun navigateToProgChart1() {
        val intent = Intent(this, ProgChart::class.java)
        startActivity(intent)
    }

    // Function to handle navigation to ProgChart2
    private fun navigateToProgChart2() {
        val intent = Intent(this, ProgChart2::class.java)
        startActivity(intent)
    }
}

