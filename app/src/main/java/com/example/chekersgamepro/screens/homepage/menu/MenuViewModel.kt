package com.example.chekersgamepro.screens.homepage.menu

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.screens.homepage.menu.model.IMenuButton
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MenuViewModel(private val buttonsList: ArrayList<IMenuButton>,
                    private val onClickButton: Observable<Int>) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val onClickState = MutableLiveData<Int>()

    init {
        this.compositeDisposable.add(
                this.onClickButton
                        .subscribeOn(Schedulers.io())
                        .subscribe{
                            onClickState.postValue(it)
                        }
        )
    }


    fun openOnlineGame(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it ==  MenuButtonsType.ONLINE_GAME.ordinal}
    }

    fun openComputerGame(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it ==  MenuButtonsType.COMPUTER_GAME.ordinal}
    }

    fun openTopPlayers(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it ==  MenuButtonsType.TOP_PLAYERS.ordinal}
    }


    fun openUpdatePicture(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .subscribeOn(Schedulers.io())
                .filter { it ==  MenuButtonsType.UPDATE_PICTURE.ordinal}
    }

    fun openShareGame(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it ==  MenuButtonsType.SHARE.ordinal}
    }

    fun openSettings(lifecycleOwner: LifecycleOwner): Observable<Int> {
        return Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onClickState))
                .filter { it ==  MenuButtonsType.SETTINGS.ordinal}
    }


    private fun notifyByClick(onClick: Int) {
        val onClickButton = -1
        when (onClick) {
            MenuButtonsType.ONLINE_GAME.ordinal -> onlineGame()
            MenuButtonsType.ONLINE_GAME.ordinal -> computerGame()
            MenuButtonsType.ONLINE_GAME.ordinal -> topPlayers()
            MenuButtonsType.ONLINE_GAME.ordinal -> share()
            MenuButtonsType.ONLINE_GAME.ordinal -> settings()
        }
        onClickState.postValue(onClickButton)
    }

    private fun settings() {

    }

    private fun share() {

    }

    private fun topPlayers() {

    }

    private fun computerGame() {

    }

    private fun onlineGame() {

    }

    fun getButtonsList() = this.buttonsList

}