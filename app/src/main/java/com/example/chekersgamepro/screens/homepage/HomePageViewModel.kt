package com.example.chekersgamepro.screens.homepage

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.screens.game.CheckersActivity
import com.example.chekersgamepro.screens.homepage.dialog.DialogStateCreator
import com.example.chekersgamepro.util.CheckersApplication
import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class HomePageViewModel : ViewModel() {

    private val repositoryManager = RepositoryManager.create()

    private val gameState = MutableLiveData<Optional<Intent>>()

    private val msgState = MutableLiveData<DialogStateCreator>()

    private val changeAvailabilityGame = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create()

    init {
        compositeDisposable.add(repositoryManager.getDataPlayerChanges().subscribe())

        compositeDisposable.add(repositoryManager.finishRequestOnlineGame().subscribe())

        compositeDisposable.add(
                repositoryManager
                        .startOnlineGame()
                        .map { DataGame.Mode.ONLINE_GAME_MODE }
                        .map(this::createCheckersGameIntent)
                        .map { Optional.of(it) }
                        .doOnNext(gameState::postValue)
                        .map { false }
                        .doOnNext(changeAvailabilityGame::postValue)
                        .flatMapCompletable { repositoryManager.setMoneyByGameResult(null) }
                        .subscribe())

        compositeDisposable.add(
                repositoryManager
                        .getMsgIfNeeded()
                        .subscribe(msgState::postValue))

    }

    private fun createCheckersGameIntent(gameMode: Int): Intent {
        val player = repositoryManager.getPlayer()
        val intent = Intent(context, CheckersActivity::class.java)

        intent.putExtra("PLAYER_ONE", getPlayerNameOne(gameMode, player))
        intent.putExtra("PLAYER_TWO", getPlayerNameTwo(gameMode, player))
        intent.putExtra("GAME_MODE", gameMode)
        return intent
    }

    private fun getPlayerNameOne(gameMode: Int, player: IPlayer): String =
            if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                if (player.isOwner()) player.getRemotePlayer() else player.getPlayerName()
            } else "COMPUTER"

    private fun getPlayerNameTwo(gameMode: Int, player: IPlayer): String =
            if (gameMode == DataGame.Mode.ONLINE_GAME_MODE) {
                if (player.isOwner()) player.getPlayerName() else player.getRemotePlayer()
            } else player.getPlayerName()

    fun setPlayerCanPlay(isCanPlay: Boolean) = repositoryManager.setIsCanPlay(isCanPlay)

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

    fun startGame(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, gameState))
                    .filter { it.isPresent }
                    .map { it.get() }

    fun changeAvailabilityGame(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, gameState))
                    .map { !it.isPresent }


    fun initComputerGame() {
        gameState.postValue(Optional.fromNullable(createCheckersGameIntent(DataGame.Mode.COMPUTER_GAME_MODE)))
    }

    fun getUserProfileMoneyChanges(): Flowable<Int> {
        return repositoryManager.getUserProfileDataChanges()
                .map { it.getMoney() }
                .doOnNext {
                    Log.d("TEST_GAME", "HomePageViewModel -> MONEY: $it")
                }
    }

    fun setFinishGame(data: Intent?): Completable = repositoryManager
            .resetPlayer()
            .doOnEvent { gameState.postValue(Optional.absent()) }
            .andThen(isNeedUpdate(data))


    private fun isNeedUpdate(data: Intent?): Completable {
        if (data == null) return Completable.complete()

        val isNeedUpdate = data.getBooleanExtra("IS_NEED_UPDATE_USER_PROFILE", false)
        if (isNeedUpdate) {
            val isYourWin = data.getBooleanExtra("IS_YOUR_WIN", false)
            return repositoryManager.setMoneyByGameResult(isYourWin)
        }

        return Completable.complete()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}