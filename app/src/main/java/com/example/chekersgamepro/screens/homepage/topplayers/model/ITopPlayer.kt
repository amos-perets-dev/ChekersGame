package com.example.chekersgamepro.screens.homepage.topplayers.model

import android.graphics.Bitmap

interface ITopPlayer {

    fun getPlayerName(): String
    fun getTotalWin(): Int
    fun getTotalLoss(): Int
    fun getMoney(): Int
    fun getAvatarEncode(): String
    fun getPlayerPositionSign(): String
}