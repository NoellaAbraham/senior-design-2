package com.example.sd2

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DoctorsAppointment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctors_appointment)

        // Book Appointment Button Clicks
        val bookAppointment1 = findViewById<Button>(R.id.bookAppointment1)
        val bookAppointment2 = findViewById<Button>(R.id.bookAppointment2)
        val bookAppointment3 = findViewById<Button>(R.id.bookAppointment3)

        bookAppointment1.setOnClickListener {
            Toast.makeText(this, "Booking appointment for Dr. Jane Doe", Toast.LENGTH_SHORT).show()
        }
        bookAppointment2.setOnClickListener {
            Toast.makeText(this, "Booking appointment for Dr. Jane Doe", Toast.LENGTH_SHORT).show()
        }
        bookAppointment3.setOnClickListener {
            Toast.makeText(this, "Booking appointment for Dr. Jane Doe", Toast.LENGTH_SHORT).show()
        }
    }
}
