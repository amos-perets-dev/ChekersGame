package com.example.chekersgamepro.screens.homepage.menu

import android.content.Intent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton
import com.example.chekersgamepro.util.IntentUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MenuViewModel(private val buttonsList: ArrayList<IMenuButton>,
                    private val onClickButton: Observable<Int>,
                    private val isCanPlay: Completable,
                    private val createPlayersGameIntent: Single<Intent>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val onClickState = MutableLiveData<Int>()

    private val computerGame = MutableLiveData<Intent>()

    init {
        this.compositeDisposable.add(
                this.onClickButton
                        .subscribeOn(Schedulers.io())
                        .subscribe(onClickState::postValue)
        )
    }

    fun openOnlinePlayers(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it == MenuButtonsType.ONLINE_PLAYERS.ordinal }
    }

    fun openComputerGame(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it == MenuButtonsType.COMPUTER_GAME.ordinal }
    }

    fun openTopPlayers(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it == MenuButtonsType.TOP_PLAYERS.ordinal }
    }

    fun openUpdatePicture(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .subscribeOn(Schedulers.io())
                .filter { it == MenuButtonsType.RULES.ordinal }
    }

    fun openSettings(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it == MenuButtonsType.SETTINGS.ordinal }
    }

    fun shareApp(lifecycleOwner: LifecycleOwner): Observable<Intent> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it == MenuButtonsType.SHARE.ordinal }
                .map { IntentUtil.createAppShareIntent() }
    }

    fun isCloseApp(lifecycleOwner: LifecycleOwner): Observable<Boolean> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .map { it == MenuButtonsType.CLOSE_APP.ordinal }
                .filter(Functions.equalsWith(true))
                .delay(350, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { CheckersApplication.create().closeAppNow("NO") }
    }

    fun startComputerGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerGame))

    fun getButtonsList() = this.buttonsList

    fun onClickSettings(ignored: View?) {
        onClickState.postValue(MenuButtonsType.SETTINGS.ordinal)
    }

    fun onClickShare(ignored: View?) {
        onClickState.postValue(MenuButtonsType.SHARE.ordinal)
    }


    fun onClickExit(ignored: View?) {
        onClickState.postValue(MenuButtonsType.CLOSE_APP.ordinal)
    }

    fun onClickComputerGame() {
        this.compositeDisposable.add(
                this.isCanPlay
                        .andThen(this.createPlayersGameIntent)
                        .subscribe { t1, t2 -> computerGame.postValue(t1) }
        )
    }

    override fun onCleared() {
        this.compositeDisposable.clear()
        super.onCleared()
    }
}