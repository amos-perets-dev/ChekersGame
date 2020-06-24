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
import com.example.chekersgamepro.models.player.data.PlayerData
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogOnlinePlayersActivity
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseViewModel
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class OnlinePlayersViewModel : OnlineBaseViewModel() {

    private val onlinePlayers = repositoryManager.getOnlinePlayersByLevel()

    private val openPlayerDetailsScreen = MutableLiveData<Pair<Intent, ActivityOptionsCompat>>()

    private val checkersImageUtil = CheckersImageUtil.create()


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

    private fun createIntentDialogOnlinePlayers(remotePlayer: PlayerData): Intent {

        val keyBase = "REMOTE_PLAYER_"

        val intent = Intent(CheckersApplication.create().applicationContext, DialogOnlinePlayersActivity::class.java)
        intent.putExtra("${keyBase}AVATAR", remotePlayer.avatarEncodeImage)
        intent.putExtra("${keyBase}NAME", remotePlayer.playerName)
        intent.putExtra("${keyBase}WIN", remotePlayer.totalWin)
        intent.putExtra("${keyBase}LOSS", remotePlayer.totalLoss)
        intent.putExtra("${keyBase}LEVEL", remotePlayer.userLevel)

        val onlinePlayer: IOnlinePlayerEvent = OnlinePlayerEventImpl(
                remotePlayer.playerName
                , remotePlayer.userLevel
                , remotePlayer.id
                , checkersImageUtil.decodeBase64Async(remotePlayer.avatarEncodeImage)
                , remotePlayer.totalWin
                , remotePlayer.totalLoss)

        val dialogStateCreator = DialogStateCreator(onlinePlayer, msgByState = "", status = RequestOnlineGameStatus.SEND_REQUEST, isOwner = true)
        intent.putExtra("DIALOG_STATE_CREATOR", remotePlayer.userLevel)
        repositoryManager.setDialogCreatorByOwner(dialogStateCreator)
        return intent

    }
}