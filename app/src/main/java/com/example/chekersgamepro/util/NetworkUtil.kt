package com.example.chekersgamepro.util

import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager


class NetworkUtil {

    private val context = CheckersApplication.create()


    public fun isAvailableNetwork() : Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}