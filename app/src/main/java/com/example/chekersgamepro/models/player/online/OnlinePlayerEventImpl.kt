package com.example.chekersgamepro.models.player.online

import android.graphics.Bitmap
import androidx.core.app.ActivityOptionsCompat
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.card.PlayerCardStateEvent
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class OnlinePlayerEventImpl(private val name: String
                            , private val level: Int
                            , private val id: Long
                            , private val encodeImageProfile: Observable<Bitmap>
                            , private val totalWin: Int
                            , private val totalLoss: Int) : IOnlinePlayerEvent {

    constructor() : this("", -1, -1, Observable.empty(), -1, -1)

    private val notifierCardState = PublishSubject.create<CardPlayerState>()


    override fun getPlayerId(): Long = this.id

    override fun onClickPlay(options: ActivityOptionsCompat): CardPlayerState {
//        notifyState(PlayerCardStateEvent.PLAY_CLICK, options)

       return CardPlayerState(getPlayerId(), PlayerCardStateEvent.PLAY_CLICK, options)
    }

    private fun notifyState(playerCardStateEvent: PlayerCardStateEvent, options: ActivityOptionsCompat) {
        this.notifierCardState.onNext(CardPlayerState(getPlayerId(), playerCardStateEvent, options))
    }

    override fun getPlayerName(): Observable<String> = Observable.just(this.name)


    override fun getLevelPlayer(): Observable<String> = Observable.just(this.level.toString())

    override fun getCardPlayerState(): Observable<CardPlayerState> =
            this.notifierCardState.hide()
//                    .distinctUntilChanged(CardPlayerState::cardState)

    override fun getImageProfile(): Observable<Bitmap> =
            this.encodeImageProfile
                    .subscribeOn(Schedulers.io())

    override fun getTotalWin(): String = this.totalWin.toString()

    override fun getTotalLoss(): String = this.totalLoss.toString()
}