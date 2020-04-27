package com.example.chekersgamepro.models.player.online

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

data class OnlinePlayerEventImpl(private val name : String
                            , private val level : Int
                            , private val id : Long
                            , private val encodeImageProfile : String) : IOnlinePlayerEvent{

    private val publishSubjectClick = PublishSubject.create<IOnlinePlayerEvent>()

    override fun getPlayerId(): Long  = this.id

    override fun onClickItem(onlinePlayerEvent: IOnlinePlayerEvent) {
        this.publishSubjectClick.onNext(onlinePlayerEvent)
    }

    override fun getPlayerName(): String = this.name

    override fun getLevelPlayer(): Int = this.level

    override fun getClick() : Observable<IOnlinePlayerEvent> =  this.publishSubjectClick.hide()

    override fun getEncodeImageProfile(): String = this.encodeImageProfile
}