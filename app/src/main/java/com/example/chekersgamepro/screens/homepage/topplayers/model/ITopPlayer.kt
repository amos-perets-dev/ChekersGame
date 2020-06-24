package com.example.chekersgamepro.screens.homepage.topplayers.model

import android.graphics.Bitmap
import io.reactivex.Observable

interface ITopPlayer {

    fun getPlayerName(): String
    fun getTotalWin(): Int
    fun getTotalLoss(): Int
    fun getMoney(): Int
    fun getAvatar(): Observable<Bitmap>
    fun getPlayerPositionSign(): String
}