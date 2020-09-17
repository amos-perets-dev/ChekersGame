package com.example.chekersgamepro.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibrateUtil {

    fun vibrateNow(context: Context, duration : Long = 1000){
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else { //deprecated in API 26
            v.vibrate(duration)
        }
    }

}