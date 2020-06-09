package com.example.chekersgamepro.screens.homepage.online.dialog

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.util.Pair
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import com.example.chekersgamepro.R
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.card.CardPlayerState
import com.example.chekersgamepro.models.player.card.PlayerCardStateEvent
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.online.OnlineBaseViewModel
import com.example.chekersgamepro.util.network.NetworkConnectivityHelper
import io.reactivex.Observable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers

class DialogPlayersViewModel(private val msgByState: Observable<String>
                             , private val dialogState: Observable<DialogState>
                             , private val remotePlayerAvatar: Observable<Bitmap>
                             , private val remotePlayerName: Observable<String>
                             , private val remotePlayerTotalGames: Observable<Pair<String, String>>
                             , private val remotePlayerLevel: Observable<String>
                             , private val context: Context
                             , private val requestGameStatus: Observable<RequestOnlineGameStatus>) : OnlineBaseViewModel() {

    private val clickOnActionsButtons = MutableLiveData<Boolean>()

    private val clickOnBackPressInvalid = MutableLiveData<Boolean>()

    private lateinit var currDialogState: DialogState

    private var isTimerFinishToCloseRequestGame = false

    init {

        val networkConnectivityHelper = NetworkConnectivityHelper(context.applicationContext as Application)

        this.compositeDisposable.add(
                networkConnectivityHelper
                        .asObservable()
                        .distinctUntilChanged()
                        .filter(Functions.equalsWith(false))
                        .doOnDispose { networkConnectivityHelper.dispose() }
                        .doOnNext { simulateOnClickActionsButtons() }
                        .flatMapCompletable { repositoryManager.resetPlayer() }
                        .subscribe()
        )

        this.compositeDisposable.add(
                repositoryManager
                        .isStillRelevantRequestGame()
                        .subscribe()
        )
    }

    fun onClickAccept() {
        Log.d("TEST_GAME", "DialogPlayersViewModel onClickAccept")
        onClickActionsButtons()
        playerCardState(CardPlayerState(PlayerCardStateEvent.ACCEPT_CLICK))
    }

    fun onClickCancel() {
        Log.d("TEST_GAME", "DialogPlayersViewModel onClickCancel")
        onClickActionsButtons()

        if (isRequestGameMsg()) {
            playerCardState(CardPlayerState(PlayerCardStateEvent.DECLINE_CLICK))
        } else if (isMsgOnly()) {
            playerCardState(CardPlayerState(PlayerCardStateEvent.SHOW_DECLINE_MSG))
        }
    }

    fun onClickCancelRequestGame() {
        onClickActionsButtons()
        playerCardState(CardPlayerState(PlayerCardStateEvent.CANCEL_REQUEST_GAME_CLICK))
    }

    fun acceptOnlineGame(): Observable<Boolean> =
            requestGameStatus
                    .doOnNext { Log.d("TEST_GAME", "DialogPlayersViewModel requestGameStatus state: ${it.name}") }
                    .map { it.ordinal == RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal }
                    .filter(Functions.equalsWith(true))

    fun getDialogState(): Observable<DialogState> =
            this.dialogState
                    .distinctUntilChanged()
                    .doOnNext { this.currDialogState = it }

    private fun onClickActionsButtons() {
        notifyOnClickActionsButtons()
    }

    private fun simulateOnClickActionsButtons(){
        notifyOnClickActionsButtons()
    }

    private fun notifyOnClickActionsButtons(){
        this.clickOnActionsButtons.postValue(true)
    }

    fun getMsgText(): Observable<String> =
            this.msgByState
                    .doOnNext {  }
                    .filter { it.isNotEmpty() }
                    .subscribeOn(Schedulers.io())

    fun getRemotePlayerAvatar(): Observable<Bitmap> =
            this.remotePlayerAvatar
                    .subscribeOn(Schedulers.io())
                    .distinctUntilChanged()

    fun getRemotePlayerLevel(): Observable<String> =
            this.remotePlayerLevel
                    .subscribeOn(Schedulers.io())
                    .distinctUntilChanged()

    fun getRemotePlayerName(): Observable<String> =
            this.remotePlayerName
                    .subscribeOn(Schedulers.io())
                    .distinctUntilChanged()

    fun getWaitingText(): Observable<String> =
            getRemotePlayerName()
                    .map { playerName -> this.context.getString(R.string.activity_home_page_online_players_dialog_waiting_text, playerName) }

    fun getMsgDuration(animateActionsButtonsDuration: Int): Observable<Long> =
            getRemotePlayerName()
                    .map { remotePlayerName ->
                        this.context.getString(R.string.activity_home_page_online_players_dialog_request_game_text, remotePlayerName)
                    }
                    .map { it.length }
                    .map {
                        val durationAnimate = this.context.resources.getInteger(R.integer.view_special_text_animate_text_duration_letter)
                        val timerDuration = (durationAnimate * it) + animateActionsButtonsDuration.toLong()
                        return@map timerDuration
                    }

    fun getRemotePlayerTotalGames(): Observable<Pair<String, String>> =
            this.remotePlayerTotalGames
                    .subscribeOn(Schedulers.io())
                    .distinctUntilChanged()

    private fun isRequestGameMsg() = this.currDialogState.ordinal == DialogState.MSG_WITH_BUTTONS.ordinal

    private fun isMsgOnly() = this.currDialogState.ordinal == DialogState.MSG_ONLY.ordinal

    fun isClickOnActionsButtons(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, this.clickOnActionsButtons))
                    .subscribeOn(Schedulers.io())
                    .filter(Functions.equalsWith(true))

    fun isClickOnBackPressInvalid(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, this.clickOnBackPressInvalid))
                    .subscribeOn(Schedulers.io())
                    .filter(Functions.equalsWith(true))

    fun timerFinishToCloseRequestGame() {
        this.isTimerFinishToCloseRequestGame = true
    }

    fun onBackPress() {

        if (isMsgOnly()){
            onClickCancel()
        }else if (isRequestGameMsg()){
            if (this.isTimerFinishToCloseRequestGame) {
                onClickCancel()
            }
        } else{
            if (this.isTimerFinishToCloseRequestGame) {
                this.clickOnBackPressInvalid.postValue(true)
            } else {
                onClickCancelRequestGame()
            }
        }

    }
}