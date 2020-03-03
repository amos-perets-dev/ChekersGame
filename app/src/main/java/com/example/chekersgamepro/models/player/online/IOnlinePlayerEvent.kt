package com.example.chekersgamepro.models.player.online

import io.reactivex.Observable

interface IOnlinePlayerEvent {

    fun getPlayerName() : String

    fun getPlayerId() : Long

    fun getLevelPlayer() : Int

    fun onClickItem(onlinePlayerEvent : IOnlinePlayerEvent)

    fun getClick(): Observable<IOnlinePlayerEvent>
}