package com.example.sd2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameStatusViewModel : ViewModel(){
    val game1Enabled = MutableLiveData<Boolean>().apply { value = true }
    val game2Enabled = MutableLiveData<Boolean>().apply { value = true }
    val game3Enabled = MutableLiveData<Boolean>().apply { value = true }
}