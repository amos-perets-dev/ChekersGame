package com.example.chekersgamepro.models.player.online

import android.graphics.Bitmap
import io.reactivex.Observable

interface IOnlinePlayerEvent {

    fun getPlayerName() : Observable<String>

    fun getPlayerId() : Long

    fun getLevelPlayer() : Observable<String>

    fun onClickItem(onlinePlayerEvent : IOnlinePlayerEvent)

    fun getClick(): Observable<IOnlinePlayerEvent>

    fun getEncodeImageProfile(): Observable<Bitmap>
}