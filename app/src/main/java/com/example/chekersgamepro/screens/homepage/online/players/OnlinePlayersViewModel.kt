package com.example.chekersgamepro.screens.homepage.online.players

import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.data.IPlayer
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogOnlinePlayersActivity
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseViewModel
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class OnlinePlayersViewModel : OnlineBaseViewModel() {

    private val onlinePlayers = repositoryManager.getOnlinePlayersByLevel()

    private val openPlayerDetailsScreen = MutableLiveData<Pair<Intent, ActivityOptionsCompat>>()

    private val checkersImageUtil = CheckersImageUtil.create()

    init {
//        compositeDisposable.add(
//                getOnlinePlayers()
//                        .switchMap { it ->
//                            Observable.fromIterable(it)
//                                    .flatMap(IOnlinePlayerEvent::getCardPlayerState)
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .doOnNext {
//
//                                        val intent = createIntentDialogOnlinePlayers(repositoryManager.getRemotePlayerById(it.playerId))
//
//                                        openPlayerDetailsScreen.postValue(Pair.create(intent, it.options))
//                                    }
//                                    .doOnNext(this::playerCardState)
//                        }
//                        .subscribe()
//        )
    }

    private fun createIntentDialogOnlinePlayers(remotePlayer: IPlayer): Intent {

        val keyBase = "REMOTE_PLAYER_"

        val intent = Intent(CheckersApplication.create().applicationContext, DialogOnlinePlayersActivity::class.java)
        intent.putExtra("${keyBase}AVATAR", remotePlayer.getAvatarEncode())
        intent.putExtra("${keyBase}NAME", remotePlayer.getPlayerName())
        intent.putExtra("${keyBase}WIN", remotePlayer.getTotalWin())
        intent.putExtra("${keyBase}LOSS", remotePlayer.getTotalLoss())
        intent.putExtra("${keyBase}LEVEL", remotePlayer.getLevelPlayer())

        val onlinePlayer: IOnlinePlayerEvent = OnlinePlayerEventImpl(
                remotePlayer.getPlayerName()
                , remotePlayer.getLevelPlayer()
                , remotePlayer.getPlayerId()
                , checkersImageUtil.decodeBase64Async(remotePlayer.getAvatarEncode())
                , remotePlayer.getTotalWin()
                , remotePlayer.getTotalLoss())

        val dialogStateCreator = DialogStateCreator(onlinePlayer, isNeedShowMessage = false, isNeedShowActionMessage = false, msgByState = "")
        intent.putExtra("DIALOG_STATE_CREATOR", remotePlayer.getLevelPlayer())
        repositoryManager.setDialogCreator(dialogStateCreator)
        return intent

    }

    fun openPlayerDetailsScreen(lifecycleOwner: LifecycleOwner): Observable<Pair<Intent, ActivityOptionsCompat>> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, openPlayerDetailsScreen))
                .subscribeOn(Schedulers.io())
    }

    fun getOnlinePlayers(): Observable<List<IOnlinePlayerEvent>> {
        return onlinePlayers
                .subscribeOn(Schedulers.io())
    }

    fun clickOnPlayerCard(cardPlayer: CardPlayerState) {
        val intent = createIntentDialogOnlinePlayers(repositoryManager.getRemotePlayerById(cardPlayer.playerId))

        openPlayerDetailsScreen.postValue(Pair.create(intent, cardPlayer.options))

        playerCardState(cardPlayer)
    }
}