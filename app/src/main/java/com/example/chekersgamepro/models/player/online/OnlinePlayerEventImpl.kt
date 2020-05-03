package com.example.chekersgamepro.models.player.online

import android.graphics.Bitmap
import com.example.chekersgamepro.checkers.CheckersImageUtil
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

data class OnlinePlayerEventImpl(private val name: String
                            , private val level: Int
                            , private val id: Long
                            , private val encodeImageProfile: Observable<Bitmap>) : IOnlinePlayerEvent{

    private val publishSubjectClick = PublishSubject.create<IOnlinePlayerEvent>()
    private val imageUtil = CheckersImageUtil.create()

    override fun getPlayerId(): Long  = this.id

    override fun onClickItem(onlinePlayerEvent: IOnlinePlayerEvent) {
        this.publishSubjectClick.onNext(onlinePlayerEvent)
    }

    override fun getPlayerName(): Observable<String> = Observable.just(this.name)

    override fun getLevelPlayer(): Observable<String> = Observable.just(this.level.toString())

    override fun getClick() : Observable<IOnlinePlayerEvent> =  this.publishSubjectClick.hide()

    override fun getEncodeImageProfile(): Observable<Bitmap> =
            this.encodeImageProfile
                    .subscribeOn(Schedulers.io())
}