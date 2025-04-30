package com.example.sd2

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface EmotionApi {
    @Multipart
    @POST("/predict_emotion")
    fun uploadImage(@Part image: MultipartBody.Part): Call<EmotionResponse>
}

data class EmotionResponse(
    val emotion: String
)
