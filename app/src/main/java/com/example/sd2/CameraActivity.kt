package com.example.sd2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var emotionText: TextView
    private lateinit var imageCapture: ImageCapture
    private val handler = Handler(Looper.getMainLooper())
    private val interval = 3000L // 3 seconds
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        emotionText = findViewById(R.id.emotionText)

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                startFrameCaptureLoop()
            } catch (e: Exception) {
                Log.e("Camera", "Error starting camera", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startFrameCaptureLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                captureAndSendFrame()
                handler.postDelayed(this, interval)
            }
        }, interval)
    }

    private fun captureAndSendFrame() {
        val photoFile = File.createTempFile("frame", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    sendImageToServer(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Capture", "Failed to capture image", exception)
                }
            }
        )
    }

    private fun sendImageToServer(file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        RetrofitClient.api.uploadImage(body).enqueue(object : Callback<EmotionResponse> {
            override fun onResponse(call: Call<EmotionResponse>, response: Response<EmotionResponse>) {
                if (response.isSuccessful) {
                    val emotion = response.body()?.emotion ?: "unknown"
                    emotionText.text = "Emotion: $emotion"
                } else {
                    emotionText.text = "Error getting emotion"
                }
            }

            override fun onFailure(call: Call<EmotionResponse>, t: Throwable) {
                emotionText.text = "Server error"
                Log.e("Upload", "Failed", t)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
