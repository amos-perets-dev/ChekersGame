package com.example.chekersgamepro.screens.homepage.online

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.card.PlayerCardStateEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import java.util.concurrent.TimeUnit

open class OnlineBaseViewModel : ViewModel() {

    protected val repositoryManager = RepositoryManager.create()

    protected val compositeDisposable = CompositeDisposable()

    protected open fun playerCardState(cardPlayerState: CardPlayerState) {
        val clickState = cardPlayerState.cardStateEvent

        when (clickState.ordinal) {
            PlayerCardStateEvent.PLAY_CLICK.ordinal -> sendRequestOnlineGame(cardPlayerState.playerId)
            PlayerCardStateEvent.DECLINE_CLICK.ordinal -> declineOnlineGame()
            PlayerCardStateEvent.CANCEL_REQUEST_GAME_CLICK.ordinal -> cancelRequestGame()
            PlayerCardStateEvent.ACCEPT_CLICK.ordinal -> acceptOnlineGame()
            PlayerCardStateEvent.SHOW_DECLINE_MSG.ordinal -> finishRequestOnlineGame()
        }
    }

    init {

//        repositoryManager
//                .checkRequestGame()
//                .subscribe {
//                    Log.d("TEST_GAME", "OnlineBaseViewModel checkRequestGame RESULT: $it")
//                }
    }

    private fun cancelRequestGame() {
        compositeDisposable.add(repositoryManager
                .cancelRequestGame()
                .subscribe())
    }

    /**
     * Call when the player(owner) sent request game to another player(guest)
     *
     * @param remotePlayer the player that the owner want to play with him
     */
    private fun sendRequestOnlineGame(remotePlayerId: Long) {
        compositeDisposable.add(repositoryManager
                .sendRequestOnlineGame(remotePlayerId)
                .subscribe()
        )
    }

    private fun finishRequestOnlineGame() {
        compositeDisposable.add(
                repositoryManager
                        .finishRequestOnlineGame()
                        .subscribe()
        )

    }

    private fun declineOnlineGame() {
        compositeDisposable.add(repositoryManager
                .declineOnlineGame()
                .subscribe()
        )
    }

    private fun acceptOnlineGame() {
        compositeDisposable.add(repositoryManager
                .acceptOnlineGame()
                .subscribe()
        )
    }

    fun isWaitingPlayer(): Observable<Boolean> = repositoryManager.isStillSendRequest().skip(1)

    override fun onCleared() {
        Log.d("TEST_GAME", "OnlineBaseViewModel -> onCleared")
        compositeDisposable.clear()
        super.onCleared()
    }
}


