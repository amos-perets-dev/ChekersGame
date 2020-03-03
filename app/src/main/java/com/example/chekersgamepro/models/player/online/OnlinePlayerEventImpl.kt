package com.example.chekersgamepro.models.player.online

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class OnlinePlayerEventImpl(private val name : String
                            , private val level : Int
                            , private val id : Long) : IOnlinePlayerEvent{

    private val publishSubjectClick = PublishSubject.create<IOnlinePlayerEvent>()

    override fun getPlayerId(): Long  = id

    override fun onClickItem(onlinePlayerEvent: IOnlinePlayerEvent) {
        publishSubjectClick.onNext(onlinePlayerEvent)
    }

    override fun getPlayerName(): String = this.name

    override fun getLevelPlayer(): Int = this.level

    override fun getClick() : Observable<IOnlinePlayerEvent> =  publishSubjectClick.hide()

}