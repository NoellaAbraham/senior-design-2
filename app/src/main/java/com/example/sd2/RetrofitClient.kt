package com.example.sd2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.105:5000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: EmotionApi = retrofit.create(EmotionApi::class.java)
}

