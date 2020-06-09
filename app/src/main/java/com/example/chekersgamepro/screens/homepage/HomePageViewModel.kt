package com.example.chekersgamepro.screens.homepage

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.screens.homepage.avatar.AvatarScreenState
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogOnlinePlayersActivity
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import com.example.chekersgamepro.util.PermissionUtil
import com.example.chekersgamepro.util.network.NetworkConnectivityHelper
import com.google.common.base.Optional
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class HomePageViewModel : ViewModel(), LifecycleObserver {

    private val repositoryManager = RepositoryManager.create()

    private val onlineGame = MutableLiveData<Optional<Intent>>()

    private val computerGame = MutableLiveData<Intent>()

    private val msgState = MutableLiveData<DialogStateCreator>()

    private val avatarScreenState = MutableLiveData<AvatarScreenState>()

    private val homePageState = MutableLiveData<HomePageState>()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create()

    private var isActiveViewModel = false

    init {
//        compositeDisposable.add(repositoryManager.finishRequestOnlineGame().subscribe())
        val networkConnectivityHelper = NetworkConnectivityHelper(context.applicationContext as Application)

//        compositeDisposable.add(
//                networkConnectivityHelper
//                        .asObservable()
//                        .doOnDispose { networkConnectivityHelper.dispose() }
//                        .flatMapCompletable { isConnected ->
//                            Observable.fromCallable { isActiveViewModel }
//                                    .filter(Functions.equalsWith(true))
//                                    .filter { isConnected }
//                                    .flatMapCompletable { repositoryManager.resetPlayer() }
//                        }
//                        .subscribe()
//        )

        compositeDisposable.add(
                repositoryManager
                        .startOnlineGame()
                        .map { DataGame.Mode.ONLINE_GAME_MODE }
                        .flatMapCompletable { gameMode ->
                            Log.d("TEST_GAME", "HomePageViewModel -> startOnlineGame -> flatMapCompletable")
                            createCheckersGameIntent(gameMode)
                                    .map { Optional.of(it) }
                                    .flatMapCompletable { intent ->
                                        repositoryManager.setMoney()
                                                .doOnEvent { onlineGame.postValue(intent) }
                                    }
                        }
                        .subscribe()
        )

        repositoryManager.startGetDataPlayerChanges()

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected fun onLifeCycleResume() {
//        repositoryManager
//                .resetPlayer()
//                .subscribe()
        isActiveViewModel = true
        Log.d("TEST_GAME", "HomePageViewModel -> onLifeCycleResume")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected fun onLifeCyclePause() {
        isActiveViewModel = false
        Log.d("TEST_GAME", "HomePageViewModel -> onLifeCyclePause")
    }

    fun getMsgState(activity: Activity): Observable<Intent> {
        return repositoryManager
                .getDialogState()
                .filter { it.msgByState.isNotEmpty() && !DialogOnlinePlayersActivity.isDialogPlayersAlreadyOpen }
                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext {
                    Log.d("TEST_GAME", "************* ${it.msgByState}")
                }
                .map { Intent(activity, DialogOnlinePlayersActivity::class.java) }
    }

    private fun createCheckersGameIntent(gameMode: Int): Single<Intent> = repositoryManager.createPlayersGame(gameMode)

    fun openAvatarScreen(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, avatarScreenState))
                    .map { it.ordinal == AvatarScreenState.OPEN_SCREEN.ordinal }
                    .filter(Functions.equalsWith(true))

    fun startOnlineGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, onlineGame))
                    .filter { it.isPresent }
                    .map { it.get() }


    fun isOpenOnlinePlayers(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, homePageState))
                    .subscribeOn(Schedulers.io())
                    .map { it.ordinal == HomePageState.OPEN_ONLINE_PLAYERS.ordinal }
                    .filter(Functions.equalsWith(true))

    fun startComputerGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, computerGame))

    fun clickOnComputerGame() {
        compositeDisposable.add(
                repositoryManager.setIsCanPlay(false)
                        .andThen(createCheckersGameIntent(DataGame.Mode.COMPUTER_GAME_MODE))
                        .subscribe { t1, t2 -> computerGame.postValue(t1) }
        )
    }

    fun getUserProfileMoneyChanges() = repositoryManager.getUserProfileMoneyChanges()

    fun getUserProfileMoney() = repositoryManager.getUserProfileMoney()

    fun getUserProfileLevelChanges() = repositoryManager.getUserProfileLevelChanges()

    fun finishGame(data: Intent): Completable = repositoryManager
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
                    .subscribeOn(Schedulers.io())

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
                            avatarScreenState.postValue(AvatarScreenState.OPEN_SCREEN)
                        }
        )
    }

    fun clickOnOnlineGame() {
        homePageState.postValue(HomePageState.OPEN_ONLINE_PLAYERS)
    }

}