package com.example.chekersgamepro.db.repository

import android.util.Log
import com.example.chekersgamepro.RegistrationStatus
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.localy.preferences.SharedPreferencesManager
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.db.remote.RemoteDbManager
import com.example.chekersgamepro.db.repository.manager.UserProfileManager
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.dialog.DialogStateCreator
import com.example.chekersgamepro.util.CheckersApplication
import com.example.chekersgamepro.util.StringUtil
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.internal.functions.Functions
import java.util.concurrent.TimeUnit

class RepositoryManager : Repository {

    private val context = CheckersApplication.create()

    private val sharedPreferencesManager = SharedPreferencesManager(context)

    private var userNameExistInvalid = HashSet<String>()

    private var userNameNotExistValid = HashSet<String>()

    private lateinit var player: IPlayer

    private val realmManager = RealmManager()

    private val remoteDb: IRemoteDb = RemoteDbManager()

    private val userProfileManager = UserProfileManager(realmManager, remoteDb)

    private var userName = ""

    init {
        Log.d("TEST_GAME", "RepositoryManager init: ${this}")
    }

    fun isRegistered(): Boolean = sharedPreferencesManager.isRegistered()

    fun setRunFirstTime() {
        sharedPreferencesManager.setRunFirstTime()
    }

    fun addNewUser(userName: String): Single<RegistrationStatus> {
        this.userName = userName
        return isUserNameExistServer(userName)
                .flatMap { isExist ->
                    if (isExist) {
                        return@flatMap Single.just(RegistrationStatus.NOT_AVAILABLE)
                    }

                    val id = StringUtil.convertToAscii(userName) + System.currentTimeMillis()

                    return@flatMap userProfileManager.createUser(id, userName)
                            .flatMap { remoteDb.createPlayer(id, userName) }
                            .doOnEvent { player, throwable ->
                                if (throwable == null) {
                                    this.player = player
                                }
                            }
                            .flatMapCompletable { userProfileManager.insertAsync()  }
                            .doOnError { Log.d("TEST_GAME", "doOnError addNewUser") }
                            .toSingleDefault(RegistrationStatus.REGISTED)
                            .onErrorReturnItem(RegistrationStatus.ERROR)
                            .doOnEvent { status, throwable ->
                                if (status.ordinal == RegistrationStatus.REGISTED.ordinal) {
//                                    sharedPreferencesManager.setIsRegistered()
                                }
                            }
                }
    }

    fun getUserProfileDataChanges(): Flowable<UserProfileImpl> {
        return userProfileManager.getUserProfileDataChanges(userName)
                .doOnNext {
                    Log.d("TEST_GAME", "RepositoryManager -> MONEY: ${it.getMoney()}")
                }
    }

    fun isUserNameExistLocallyListInvalid(userName: String): Boolean {
        return userNameExistInvalid.contains(userName)
    }

    fun isUserNameExistLocallyListValid(userName: String): Boolean {
        return userNameNotExistValid.contains(userName)
    }

    fun isUserNameExist(userName: String): Single<Boolean> {

        if (isUserNameExistLocallyListInvalid(userName)) {
            return Single.just(true)
        }

        if (isUserNameExistLocallyListValid(userName)) {
            return Single.just(false)
        }

        return isUserNameExistServer(userName)

    }

    private fun isUserNameExistServer(userName: String): Single<Boolean> {
        return remoteDb.isUserNameExistServer(userName)
                .doOnEvent { isExist, t2 ->
                    if (isExist) {
                        userNameExistInvalid.add(userName)
                    } else {
                        userNameNotExistValid.add(userName)
                    }
                }
    }

    fun getDataPlayerChanges(): Observable<IPlayer> =
            remoteDb.getDataPlayerChanges()
                    .doOnNext { this.player = it }

    fun setIsCanPlay(isCanPlay: Boolean): Completable = remoteDb.setIsCanPlay(isCanPlay)


    fun getOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>> =
            remoteDb.getAllAvailableOnlinePlayersByLevel(this.player.getPlayerName(), this.player.getLevelPlayer())

    fun sendRequestOnlineGame(remotePlayerId: Long): Completable =
            remoteDb.sendRequestOnlineGame(remotePlayerId)

    fun declineOnlineGame(): Completable = remoteDb.declineOnlineGame()

    fun acceptOnlineGame(): Completable = remoteDb.acceptOnlineGame()

    fun getMsgIfNeeded() : Observable<DialogStateCreator>{
        return Observable.combineLatest(getRequestGameMsgText(), isNeedShowMessage(), isNeedShowActionMessage(),
                Function3 { msg : String, isNeedShowMessage : Boolean, isNeedShowActionMessage : Boolean -> DialogStateCreator(msg, isNeedShowMessage, isNeedShowActionMessage) })
    }

    private fun getRequestGameMsgText(): Observable<String> = remoteDb.getRequestGameMsgText().distinctUntilChanged()

    private fun isNeedShowMessage(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .map {
                        it.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.isOwner()
                                || it.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.isOwner()
                    }
                    .distinctUntilChanged()

    private fun isNeedShowActionMessage(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .map { it.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.isOwner() }
                    .distinctUntilChanged()

    fun finishRequestOnlineGame(): Completable =
            remoteDb.getRequestGameStatus()
                    .filter { status -> status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.isOwner() }
                    .delay(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .flatMapCompletable { remoteDb.finishRequestOnlineGame() }

    fun startOnlineGame(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .map{ status -> status.ordinal == RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal}
                    .filter(Functions.equalsWith(true))

    fun getNowPlay(): Observable<Int> = remoteDb.getNowPlayer()

    fun setEndTurn() : Completable = remoteDb.setNowPlayer()

    fun getRemoteMove(): Observable<RemoteMove> = remoteDb.getRemoteMove()

    fun getPlayer() : IPlayer = this.player

    fun notifyEndTurn(move: RemoteMove) : Completable = remoteDb.notifyEndTurn(move)

    fun setMoneyByGameResult(isYourWin: Boolean?): Completable  = userProfileManager.setMoney(isYourWin)


    fun setFinishGameTechnicalLoss() : Completable = remoteDb.setFinishGameTechnicalLoss()

    fun isTechnicalWin() : Observable<Boolean> = remoteDb.isTechnicalWin()

    fun resetPlayer() : Completable = remoteDb.resetPlayer()

    companion object Factory {

        private var repositoryManager: RepositoryManager? = null

        @JvmStatic
        fun create(): RepositoryManager {
            if (repositoryManager == null) {
                repositoryManager = RepositoryManager()
            }
            return repositoryManager as RepositoryManager
        }
    }

}