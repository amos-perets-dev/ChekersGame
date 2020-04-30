package com.example.chekersgamepro.screens.homepage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.screens.homepage.dialog.DialogStateCreator
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.util.PermissionUtil
import com.google.common.base.Optional
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import java.util.concurrent.TimeUnit

class HomePageViewModel : ViewModel() {

    private val repositoryManager = RepositoryManager.create()

    private val onlineGame = MutableLiveData<Optional<Intent>>()

    private val computerGame = MutableLiveData<Intent>()

    private val msgState = MutableLiveData<DialogStateCreator>()

    private val openAvatarScreen = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create()

    init {
        compositeDisposable.add(repositoryManager.finishRequestOnlineGame().subscribe())

        compositeDisposable.add(
                repositoryManager
                        .startOnlineGame()
                        .map { DataGame.Mode.ONLINE_GAME_MODE }
                        .flatMapCompletable { gameMode ->
                            createCheckersGameIntent(gameMode)
                                    .map { Optional.of(it) }
                                    .flatMapCompletable { intent ->
                                        repositoryManager.setMoney()
                                                .doOnEvent { onlineGame.postValue(intent) }
                                    }
                        }
                        .subscribe()
        )

        compositeDisposable.add(
                repositoryManager
                        .getMsgIfNeeded()
                        .subscribe(msgState::postValue))

        repositoryManager.startGetDataPlayerChanges()

    }

    private fun createCheckersGameIntent(gameMode: Int): Single<Intent> = repositoryManager.createPlayersGame(gameMode)

    fun getOnlinePlayers(): Observable<List<IOnlinePlayerEvent>> = repositoryManager.getOnlinePlayersByLevel()

    fun declineOnlineGame() {
        compositeDisposable.add(repositoryManager
                .declineOnlineGame()
                .subscribe())
    }

    fun acceptOnlineGame() {
        compositeDisposable.add(repositoryManager
                .acceptOnlineGame()
                .subscribe())
    }

    /**
     * Call when the player(owner) sent request game to another player(guest)
     *
     * @param remotePlayer the player that the owner want to play with him
     */
    fun sendRequestOnlineGame(remotePlayerId: Long) {
        compositeDisposable.add(repositoryManager
                .sendRequestOnlineGame(remotePlayerId)
                .subscribe())
    }

    fun getMsgState(lifecycleOwner: LifecycleOwner): Observable<DialogStateCreator> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, msgState))

    fun openAvatarScreen(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, openAvatarScreen))

    fun startOnlineGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onlineGame))
                    .filter { it.isPresent }
                    .map { it.get() }


    fun startComputerGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerGame))

    fun initComputerGame() {
        compositeDisposable.add(
                repositoryManager.setIsCanPlay(false)
                        .andThen(createCheckersGameIntent(DataGame.Mode.COMPUTER_GAME_MODE))
                        .subscribe { t1, t2 -> computerGame.postValue(t1) }
        )
    }


    fun getUserProfileMoneyChanges() = repositoryManager.getUserProfileMoneyChanges()

    fun getUserProfileMoney() = repositoryManager.getUserProfileMoney()

    fun getUserProfileLevelChanges() = repositoryManager.getUserProfileLevelChanges()

    fun setFinishGame(data: Intent): Completable = repositoryManager
            .resetPlayer()
            .andThen(isNeedUpdate(data))


    private fun isNeedUpdate(data: Intent): Completable {

        val isNeedUpdate = data.getBooleanExtra("IS_NEED_UPDATE_USER_PROFILE", false)
        val isYourWin = data.getBooleanExtra("IS_YOUR_WIN", false)

        return repositoryManager.setMoney((isNeedUpdate && isYourWin))
    }

    fun isDefaultImage(): Maybe<Boolean> =
            Single.just(repositoryManager.isDefaultImage())
                    .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter(Functions.equalsWith(true))

    fun getPlayerName(): Observable<String> = repositoryManager.getPlayerNameAsync()

    fun getImageProfileAsync(): Flowable<Bitmap?> = repositoryManager.getImageProfile()

    fun getImageProfile(): Single<Bitmap?> = repositoryManager.getImageProfile().firstOrError()

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun clickOnAvatar(context: Context) {
        compositeDisposable.add(
                PermissionUtil
                        .isStorageAndCameraPermissionGranted(context)
                        .subscribe {
                            openAvatarScreen.postValue(true)
                        }
        )
    }
}