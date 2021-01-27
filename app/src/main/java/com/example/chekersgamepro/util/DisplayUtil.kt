package com.example.chekersgamepro.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics

class DisplayUtil {

    companion object{

        fun convertDpToPixel(dp: Float): Float {
            val metrics = Resources.getSystem().displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun getDisplayMatrix(activity: Activity?): DisplayMetrics {
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            return displayMetrics
        }

        fun getHightDisplayMatrix(activity: Activity) = getDisplayMatrix(activity).heightPixels


        fun getWidthDisplayMatrix(activity: Activity) =  getDisplayMatrix(activity).widthPixels

    }

}