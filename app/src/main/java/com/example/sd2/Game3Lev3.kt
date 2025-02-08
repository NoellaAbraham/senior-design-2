package com.example.sd2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import com.example.sd2.databinding.ActivityGame3Lev3Binding
import com.example.sd2.facedetector.FaceDetectionActivity

enum class Action {
    FACE_DETECTION
}

class Game3Lev3 : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityGame3Lev3Binding
    private var action = Action.FACE_DETECTION

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGame3Lev3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageButton7 = findViewById<ImageButton>(R.id.home_button)

        imageButton7.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }

        binding.buttonFaceDetect.setOnClickListener {
            this.action = Action.FACE_DETECTION
            requestCameraAndStart()
        }
    }

    private fun requestCameraAndStart() {
        if (isPermissionGranted(cameraPermission)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun startCamera() {
        when (action) {
            Action.FACE_DETECTION -> FaceDetectionActivity.startActivity(this)
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest(
                    positive = { openPermissionSetting() }
                )
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }



}