package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        val game1 = findViewById<LinearLayout>(R.id.game1Card)
        val game2 = findViewById<LinearLayout>(R.id.game2Card)
        val game3 = findViewById<LinearLayout>(R.id.game3Card)

        game1.setOnClickListener {
            // TODO: Launch Game 1 Activity
            Toast.makeText(this, "Opening Game 1", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, Game1Activity::class.java))
        }

        game2.setOnClickListener {
            Toast.makeText(this, "Opening Game 2", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, Game2Activity::class.java))
        }

        game3.setOnClickListener {
            Toast.makeText(this, "Opening Game 3", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, Game3Activity::class.java))
        }
    }
}
