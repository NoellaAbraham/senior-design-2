package com.example.sd2

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class FocusPopupDialogFragment(private val nextActivity: Class<*>) : DialogFragment() {

    private lateinit var countdownText: TextView
    private lateinit var startButton: Button
    private var countdownTimer: CountDownTimer? = null
    private var timeLeft: Long = 15000 // 15 seconds
    private var timerRunning = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_focus_popup, null)

        countdownText = view.findViewById(R.id.countdownText)
        startButton = view.findViewById(R.id.startGameButton)

        startButton.visibility = Button.GONE

        // Start playing sound immediately
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.focus_sound)
        mediaPlayer?.start()

        startCountdown()

        startButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            startActivity(Intent(requireContext(), nextActivity))
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()
    }

    private fun startCountdown() {
        if (!timerRunning) {
            countdownTimer = object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                    countdownText.text = "Please focus... ${millisUntilFinished / 1000}s"
                }

                override fun onFinish() {
                    timerRunning = false
                    countdownText.text = "You're ready!"
                    startButton.visibility = Button.VISIBLE
                }
            }.start()
            timerRunning = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        fun newInstance(nextActivity: Class<*>): FocusPopupDialogFragment {
            return FocusPopupDialogFragment(nextActivity)
        }
    }
}
