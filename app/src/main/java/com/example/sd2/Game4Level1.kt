package com.example.sd2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Game4Level1 : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var videoView: VideoView
    private lateinit var timerText: TextView
    private lateinit var pauseButton: ImageButton
    private lateinit var playButton: ImageButton
    private lateinit var menuIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private val handler = Handler(Looper.getMainLooper())
    private val interval = 3000L

    private var startTime = 0L
    private var isTimerRunning = true
    private var cameraReady = false

    private val emotionSequence = listOf("happy", "sad", "neutral", "angry", "surprise", "fear")
    private val emotionTimes = mutableMapOf<String, Int>()
    private var currentEmotionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game4_level1)

        previewView = findViewById(R.id.previewView)
        videoView = findViewById(R.id.emotionVideo)
        timerText = findViewById(R.id.timerText)
        pauseButton = findViewById(R.id.pauseButton)
        playButton = findViewById(R.id.playButton)
        menuIcon = findViewById(R.id.menuIcon)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            startCamera()
        }

        pauseButton.setOnClickListener { isTimerRunning = false }
        playButton.setOnClickListener { isTimerRunning = true }

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, WelcomeActivity::class.java))
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardTest::class.java))
                R.id.nav_chatbot -> startActivity(Intent(this, Chatbot::class.java))
                R.id.nav_appointments -> startActivity(Intent(this, DoctorsAppointment::class.java))
                R.id.nav_resources -> startActivity(Intent(this, ResourcesActivity::class.java))
                R.id.nav_feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.nav_logout -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        loadEmotionVideo(emotionSequence[currentEmotionIndex])
        startTimer()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                cameraReady = true
                startFrameCaptureLoop()

            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startFrameCaptureLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isTimerRunning && cameraReady) captureAndSendFrame()
                handler.postDelayed(this, interval)
            }
        }, interval)
    }

    private fun captureAndSendFrame() {
        if (!::imageCapture.isInitialized) {
            Log.e("Capture", "ImageCapture not initialized")
            return
        }

        val photoFile = File.createTempFile("frame", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
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
                val detected = response.body()?.emotion ?: "unknown"
                val expected = emotionSequence[currentEmotionIndex]
                val reactionTime = getElapsedTime().toInt()

                timerText.text = "Time: ${reactionTime}s | Detected: $detected"

                if (detected.equals(expected, ignoreCase = true)) {
                    emotionTimes[expected] = reactionTime
                    timerText.setTextColor(getColor(R.color.white))
                    timerText.text = "✅ $expected detected in ${reactionTime}s"

                    if (currentEmotionIndex == emotionSequence.lastIndex) {
                        handler.postDelayed({
                            saveAllEmotionTimes {
                                startActivity(Intent(this@Game4Level1, WelcomeActivity::class.java))
                                finish()
                            }
                        }, 3000)
                    } else {
                        handler.postDelayed({
                            currentEmotionIndex++
                            loadEmotionVideo(emotionSequence[currentEmotionIndex])
                            startTimer()
                        }, 3000)
                    }
                }

            }

            override fun onFailure(call: Call<EmotionResponse>, t: Throwable) {
                timerText.text = "❌ Network Error"
            }
        })
    }

    private fun saveAllEmotionTimes(onComplete: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userID = (application as MyApp).userID
                val gameID = 4
                val levelIDs = listOf(41, 42, 43, 44, 45, 46)

                var index = 0
                for ((emotion, time) in emotionTimes) {
                    val levelID = levelIDs.getOrNull(index) ?: continue
                    val url = URL("http://192.168.0.105/seniordes/g1l1test.php")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doOutput = true
                    connection.requestMethod = "POST"

                    val postData = "userID=$userID&gameID=$gameID&levelID=$levelID&emotion=$emotion&time=$time"
                    connection.outputStream.write(postData.toByteArray(Charsets.UTF_8))

                    connection.responseCode
                    connection.disconnect()

                    index++
                }

                runOnUiThread { onComplete() }

            } catch (e: Exception) {
                Log.e("SaveError", "Failed to save results", e)
            }
        }
    }

    private fun loadEmotionVideo(emotion: String) {
        val videoResId = when (emotion.lowercase()) {
            "happy" -> R.raw.happy_lev0
            "sad" -> R.raw.sad_lev0
            "neutral" -> R.raw.happy_lev0
            "angry" -> R.raw.angry_levl0
            "surprise" -> R.raw.surprise_lev0
            "fear" -> R.raw.fear_level0
            else -> return
        }

        videoView.setVideoPath("android.resource://$packageName/$videoResId")
        videoView.start()
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
    }

    private fun getElapsedTime(): Long {
        return (System.currentTimeMillis() - startTime) / 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
