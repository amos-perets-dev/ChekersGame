package com.example.chekersgamepro.models.player.online

import android.graphics.Bitmap
import androidx.core.app.ActivityOptionsCompat
import com.example.chekersgamepro.models.player.card.CardPlayerState
import io.reactivex.Observable

interface IOnlinePlayerEvent {

    fun getPlayerName() : Observable<String>

    fun getPlayerId() : Long

    fun getLevelPlayer() : Observable<String>

    fun onClickPlay(options: ActivityOptionsCompat): CardPlayerState

    fun getCardPlayerState(): Observable<CardPlayerState>

    fun getImageProfile(): Observable<Bitmap>

    fun getTotalWin() : String

    fun getTotalLoss() : String
}