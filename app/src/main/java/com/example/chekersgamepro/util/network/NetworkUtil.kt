package com.example.chekersgamepro.util.network

import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.example.chekersgamepro.checkers.CheckersApplication


class NetworkUtil {

    private val context = CheckersApplication.create()


    public fun isAvailableNetwork() : Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}