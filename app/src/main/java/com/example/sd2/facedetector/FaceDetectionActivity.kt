package com.example.sd2.facedetector

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.sd2.CameraXViewModel
import com.example.sd2.databinding.ActivityFaceDetectionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.DataOutputStream

class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel by viewModels<CameraXViewModel>()
    private lateinit var faceDetector: FaceDetector
    private lateinit var tfliteInterpreter: Interpreter

    private var detectedFaces = mutableListOf<Pair<Bitmap, Rect>>()

    private val emotions = listOf("angry", "disgust", "fear", "happy", "neutral", "sad", "surprise")
    private val emotionConfidenceMap = mutableMapOf<String, Float>()
    private var currentRequestedEmotion = emotions.random()
    private var previousEmotionIndex: Int? = null
    private var currentEmotionIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tfliteInterpreter = Interpreter(loadModelFile())

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraXViewModel.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }

        faceDetector = FaceDetection.getClient()

        emotions.forEach { emotion ->
            emotionConfidenceMap[emotion] = 0.0f
        }
        updateEmotionRequestText()

        binding.analyzeButton.setOnClickListener {
            if (detectedFaces.isNotEmpty()) {
                val (faceBitmap, boundingBox) = detectedFaces.last()
                displayDetectedFace(faceBitmap)
                analyzeFace(faceBitmap)
                sendEmotionDataToServer()
            } else {
                Log.d(TAG, "No faces detected to analyze")
            }
        }

    }

    private fun bindCameraPreview() {
        try {
            cameraPreview = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()
            cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
            Log.d(TAG, "Camera preview bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "binding camera preview error: ${e.message}")
        }
    }

    private fun bindInputAnalyser() {
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(faceDetector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
            Log.d(TAG, "Image analysis bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "binding image analysis error: ${e.message}")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val grayscaleBitmap = mediaImageToGrayscaleBitmap(mediaImage)
        val rotatedBitmap = rotateBitmap(grayscaleBitmap, imageProxy.imageInfo.rotationDegrees)

        val inputImage = InputImage.fromBitmap(rotatedBitmap, 0)

        detector.process(inputImage).addOnSuccessListener { faces ->
            binding.graphicOverlay.clear()
            detectedFaces.clear()

            faces.forEach { face ->
                val faceBitmap = cropFaceFromImage(rotatedBitmap, face.boundingBox, rotatedBitmap.width, rotatedBitmap.height)
                detectedFaces.add(Pair(faceBitmap, face.boundingBox))
                val faceBox = FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                binding.graphicOverlay.add(faceBox)
            }
        }
            .addOnFailureListener { e ->
                Log.e(TAG, "Face detection failed: ${e.message}")
                e.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun displayDetectedFace(faceBitmap: Bitmap) {
        binding.facePreview.setImageBitmap(faceBitmap)
        Log.d(TAG, "Displaying detected face in preview")
    }

    private fun analyzeFace(faceBitmap: Bitmap) {
        val emotionResults = runTensorFlowLite(faceBitmap)
        displayEmotionResults(emotionResults)
        captureCurrentEmotionValue(emotionResults)
    }

    private fun captureCurrentEmotionValue(emotionResults: FloatArray) {
        currentEmotionIndex?.let { currentIndex ->
            previousEmotionIndex?.let { previousIndex ->
                emotionConfidenceMap[emotions[previousIndex]] = emotionResults[previousIndex] * 100
            }

            previousEmotionIndex = currentIndex

            previousEmotionIndex?.let { prevIndex ->
                val logText = "Detected ${emotions[prevIndex]} confidence: %.2f%%".format(emotionConfidenceMap[emotions[prevIndex]]!!)
                binding.logTextView.text = logText
                Log.d(TAG, logText)
            }
        }

        updateEmotionRequestText()
    }

    private fun updateEmotionRequestText() {
        currentRequestedEmotion = emotions.random()
        currentEmotionIndex = emotions.indexOf(currentRequestedEmotion)
        binding.emotionRequestText.text = "What does $currentRequestedEmotion look like?"
    }

    private fun mediaImageToGrayscaleBitmap(mediaImage: Image): Bitmap {
        val yBuffer = mediaImage.planes[0].buffer
        val width = mediaImage.width
        val height = mediaImage.height

        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val yValue = yBuffer.get(y * width + x).toInt() and 0xFF
                val grayColor = Color.argb(255, yValue, yValue, yValue)
                pixels[y * width + x] = grayColor
            }
        }
        grayscaleBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return grayscaleBitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropFaceFromImage(bitmap: Bitmap, boundingBox: Rect, originalWidth: Int, originalHeight: Int): Bitmap {
        val scaleX = bitmap.width.toFloat() / originalWidth
        val scaleY = bitmap.height.toFloat() / originalHeight

        val left = (boundingBox.left * scaleX).toInt()
        val top = (boundingBox.top * scaleY).toInt()
        val right = (boundingBox.right * scaleX).toInt()
        val bottom = (boundingBox.bottom * scaleY).toInt()

        val adjustedLeft = max(0, left)
        val adjustedTop = max(0, top)
        val adjustedRight = min(bitmap.width, right)
        val adjustedBottom = min(bitmap.height, bottom)

        val width = adjustedRight - adjustedLeft
        val height = adjustedBottom - adjustedTop

        // make crop 48x48 pixels
        val size = min(width, height)
        val cropLeft = adjustedLeft + (width - size) / 2
        val cropTop = adjustedTop + (height - size) / 2

        if (size > 0 && cropLeft >= 0 && cropTop >= 0 && (cropLeft + size) <= bitmap.width && (cropTop + size) <= bitmap.height) {
            return Bitmap.createBitmap(bitmap, cropLeft, cropTop, size, size)
        } else {
            Log.e(TAG, "Failed to crop face image from bounding box, invalid crop dimensions")
            return bitmap
        }
    }

    private fun runTensorFlowLite(bitmap: Bitmap): FloatArray {
        val modelInputSize = 48
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)

        val inputBuffer = ByteBuffer.allocateDirect(modelInputSize * modelInputSize * 1 * 4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
            val pixels = IntArray(modelInputSize * modelInputSize)
            resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
            for (pixelValue in pixels) {
                val gray = (pixelValue shr 16 and 0xFF).toFloat() / 255.0f
                putFloat(gray)
            }
        }
        val outputTensorShape = tfliteInterpreter.getOutputTensor(0).shape()
        val outputSize = outputTensorShape[1]

        val outputBuffer = ByteBuffer.allocateDirect(outputSize * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        tfliteInterpreter.run(inputBuffer, outputBuffer)

        val output = FloatArray(outputSize)
        outputBuffer.rewind()
        outputBuffer.asFloatBuffer().get(output)

        return output
    }

    private fun displayEmotionResults(emotionResults: FloatArray) {
        val resultString = buildString {
            append("Emotion Results:\n")
            emotions.forEachIndexed { index, emotion ->
                val confidence = (emotionResults.getOrNull(index) ?: 0.0f) * 100
                emotionConfidenceMap[emotion] = confidence
                append("$emotion: %.1f%%\n".format(confidence))
            }
        }

        binding.emotionResultsText.text = resultString
        Log.d(TAG, resultString)

        Log.d(TAG, "Emotion Confidence Map: $emotionConfidenceMap")
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("ml/FERmodel.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun sendEmotionDataToServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.132.103/seniordes/emotionSet.php")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // Prepare POST data
                val userID = "7" // Hardcoded user ID
                val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val prompt = emotions.getOrNull(previousEmotionIndex ?: -1)

                // Construct POST data string
                val postData = StringBuilder()
                postData.append("userID=").append(URLEncoder.encode(userID, "UTF-8"))
                postData.append("&time=").append(URLEncoder.encode(timeStamp, "UTF-8"))
                postData.append("&date=").append(URLEncoder.encode(date, "UTF-8"))
                postData.append("&prompt=").append(URLEncoder.encode(prompt, "UTF-8"))
                emotions.forEach { emotion ->
                    val confidence = emotionConfidenceMap[emotion] ?: 0.0f
                    postData.append("&").append(emotion).append("=").append(confidence)
                }

                DataOutputStream(urlConnection.outputStream).use { outputStream ->
                    outputStream.writeBytes(postData.toString())
                    outputStream.flush()
                }

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseMessage = urlConnection.inputStream.bufferedReader().readText()
                    Log.d(TAG, "Server response: $responseMessage")
                } else {
                    Log.e(TAG, "Error response from server: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Exception while sending data to server: ${e.message}")
            }
        }
    }


    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
        fun startActivity(context: Context) {
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}
