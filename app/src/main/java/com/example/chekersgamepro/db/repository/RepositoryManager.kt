package com.example.chekersgamepro.db.repository

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.localy.preferences.SharedPreferencesManager
import com.example.chekersgamepro.db.localy.realm.RealmManager
import com.example.chekersgamepro.db.remote.IRemoteDb
import com.example.chekersgamepro.db.remote.RemoteDbManager
import com.example.chekersgamepro.db.repository.manager.PlayerManager
import com.example.chekersgamepro.db.repository.manager.UserProfileManager
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.dialog.DialogStateCreator
import com.example.chekersgamepro.screens.registration.RegistrationStatus
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.util.IntentUtil
import com.example.chekersgamepro.util.StringUtil
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class RepositoryManager : Repository {

    private val context = CheckersApplication.create()

    private val checkersConfiguration = CheckersConfiguration.getInstance()

    private val imageUtil = CheckersImageUtil.create()

    private val sharedPreferencesManager = SharedPreferencesManager(context)

    private var userNameExistInvalid = HashSet<String>()

    private var userNameNotExistValid = HashSet<String>()

    private val realmManager = RealmManager()

    private val remoteDb: IRemoteDb = RemoteDbManager(realmManager.getDefaultRealm().where(UserProfileImpl::class.java).findFirst())

    private val userProfileManager = UserProfileManager(realmManager, remoteDb)
    private val playerManager = PlayerManager(realmManager, remoteDb)

    private var imageProfileTmpEncodeBase: String? = null

    init {
        Log.d("TEST_GAME", "RepositoryManager init: $this")
    }

    fun isRegistered(): Boolean = sharedPreferencesManager.isRegistered()

    fun setRunFirstTime() {
        sharedPreferencesManager.setRunFirstTime()
    }

    private fun getEncodeImageDefaultPreUpdate(): String? = sharedPreferencesManager.getEncodeImageDefaultPreUpdate()

    override fun addNewUser(userName: String): Single<RegistrationStatus> = isUserNameExistServer(userName)
            .flatMap { isExist ->
                if (isExist) Single.just(RegistrationStatus.NOT_AVAILABLE)

                val id = StringUtil.convertToAscii(userName) + System.currentTimeMillis()

                return@flatMap userProfileManager.createUser(id, userName, getEncodeImageDefaultPreUpdate()!!)
                        .andThen(playerManager.createPlayer(id, userName, getEncodeImageDefaultPreUpdate()!!))
                        .doOnError { Log.d("TEST_GAME", "doOnError addNewUser: ${it.message}") }
                        .toSingleDefault(RegistrationStatus.REGISTED)
                        .onErrorReturnItem(RegistrationStatus.ERROR)
                        .doOnEvent { status, throwable ->
                            if (status.ordinal == RegistrationStatus.REGISTED.ordinal) {
                                sharedPreferencesManager.setIsRegistered()
                            }
                        }
            }

    fun isUserNameExistLocallyListInvalid(userName: String): Boolean = userNameExistInvalid.contains(userName)

    fun isUserNameExistLocallyListValid(userName: String): Boolean = userNameNotExistValid.contains(userName)


    fun isUserNameExist(userName: String): Single<Boolean> {

        if (isUserNameExistLocallyListInvalid(userName)) return Single.just(true)

        if (isUserNameExistLocallyListValid(userName)) return Single.just(false)

        return isUserNameExistServer(userName)

    }

    private fun isUserNameExistServer(userName: String): Single<Boolean> =
            remoteDb.isUserNameExistServer(userName)
                    .doOnEvent { isExist, t2 ->
                        if (isExist) {
                            userNameExistInvalid.add(userName)
                        } else {
                            userNameNotExistValid.add(userName)
                        }
                    }


    fun setIsCanPlay(isCanPlay: Boolean): Completable = playerManager.setIsCanPlay(isCanPlay)

    fun getOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>> = remoteDb.getAllAvailableOnlinePlayersByLevel()

    fun sendRequestOnlineGame(remotePlayerId: Long): Completable = playerManager.sendRequestOnlineGame(remotePlayerId).ignoreElement()

    fun declineOnlineGame(): Completable = playerManager.declineOnlineGame()

    fun acceptOnlineGame(): Completable = playerManager.acceptOnlineGame()

    fun getMsgIfNeeded(): Observable<DialogStateCreator> =
            Observable.combineLatest(getRequestGameMsgText(), isNeedShowMessage(), isNeedShowActionMessage(),
                    Function3 { msg: String, isNeedShowMessage: Boolean, isNeedShowActionMessage: Boolean ->
                        DialogStateCreator(msg, isNeedShowMessage, isNeedShowActionMessage)
                    })

    private fun getRequestGameMsgText(): Observable<String> = remoteDb.getRequestGameMsgText().distinctUntilChanged()

    private fun isNeedShowMessage(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .flatMap { requestGame ->
                        isOwnerPlayerAsync()
                                ?.map { isOwner ->
                                    requestGame.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !isOwner
                                            || requestGame.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && isOwner
                                }
                    }
                    .distinctUntilChanged()

    private fun isNeedShowActionMessage(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .flatMap { requestGame ->
                        isOwnerPlayerAsync()
                                ?.map { isOwner ->
                                    requestGame.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !isOwner
                                }
                    }
                    .distinctUntilChanged()

    fun finishRequestOnlineGame(): Completable =
            remoteDb.getRequestGameStatus()
                    .flatMap { requestGame ->
                        isOwnerPlayerAsync()
                                ?.filter { isOwner -> requestGame.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && isOwner }
                    }
                    .delay(2000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .flatMapCompletable { remoteDb.finishRequestOnlineGame() }

    fun isOwnerPlayerAsync() = playerManager.isOwnerPlayerAsync()

    fun startOnlineGame(): Observable<Boolean> =
            remoteDb.getRequestGameStatus()
                    .map { status -> status.ordinal == RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal }
                    .filter(Functions.equalsWith(true))

    fun getRemoteMove(): Observable<RemoteMove> = playerManager.getRemoteMove()

    fun getPlayer(): Single<IPlayer?> = playerManager.getPlayer()

    fun getPlayerNameAsync(): Observable<String> = playerManager.getPlayerNameAsync()

    fun getNowPlayAsync(): Observable<Int> = playerManager.getNowPlayAsync()

    fun notifyEndTurn(move: RemoteMove): Completable = remoteDb.notifyEndTurn(move)

    fun setMoney(isYourWin: Boolean? = null): Completable = userProfileManager.setMoney(isYourWin)

    fun setFinishGameTechnicalLoss(): Completable = remoteDb.setFinishGameTechnicalLoss()

    fun isTechnicalWin(): Observable<Boolean> = remoteDb.isTechnicalWin()

    fun resetPlayer(): Completable = playerManager.resetPlayer()

    fun startGetDataPlayerChanges() {
        playerManager.startGetDataPlayerChanges()
    }

    fun isDefaultImage() = sharedPreferencesManager.isDefaultImage()

    override fun storeImage(): Completable {

        val image = Observable.fromCallable { getImageProfileTmp() }

//        image
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .flatMapCompletable {
        return userProfileManager.setEncodeImageProfile(getImageProfileTmp())
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(sharedPreferencesManager.setIsDefaultImage())
                .observeOn(Schedulers.io())
                .andThen(getPlayerNameAsync()
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable { playerName ->
                            remoteDb.setImageProfileAndPlayer(getImageProfileTmp()!!, playerName)
                        })

//                }
//                .subscribe()

//        getPlayerNameAsync()
//                .subscribeOn(Schedulers.io())
//                .flatMapCompletable {playerName ->
//                    remoteDb.setImageProfileAndPlayer(getImageProfileTmp()!!, playerName)
//                }
//                .subscribe()
//                .flatMapCompletable { playerName ->
//                    Observable.fromCallable { getImageProfileTmp() }
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .flatMapCompletable {newImageProfileEncodeBase ->
//                                //                                val encodeImage = imageUtil.encodeBase64Image(newImageProfileEncodeBase)
//                                userProfileManager.setEncodeImageProfile(newImageProfileEncodeBase)
////                                        .andThen(remoteDb.setImageProfileAndPlayer(newImageProfileEncodeBase, playerName)
////                                                .andThen(sharedPreferencesManager.setIsDefaultImage())
////                                        )
////                                Completable.complete()
//
//                            }
//                }.subscribe()


//        getPlayerNameAsync()
//                .subscribeOn(Schedulers.io())
//                .flatMapCompletable { playerName ->
//                    Observable.fromCallable { getImageProfileTmp() }
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .flatMapCompletable {newImageProfileEncodeBase ->
////                                val encodeImage = imageUtil.encodeBase64Image(newImageProfileEncodeBase)
//                                userProfileManager.setEncodeImageProfile(newImageProfileEncodeBase)
////                                        .andThen(remoteDb.setImageProfileAndPlayer(newImageProfileEncodeBase, playerName)
////                                                .andThen(sharedPreferencesManager.setIsDefaultImage())
////                                        )
////                                Completable.complete()
//
//                            }
//                }.subscribe()
    }

    fun getUserProfileMoneyChanges(): Observable<Int> = userProfileManager.getUserProfileMoneyChanges()

    fun getUserProfileLevelChanges(): Flowable<String> =
            userProfileManager.getUserProfileDataChanges()
                    .map { it.getLevelUser().toString() }


    fun getUserProfileMoney(): Single<Int> = userProfileManager.getUserProfileMoney()

    fun getImageProfile(): Flowable<Bitmap?> = userProfileManager.getEncodeImageProfile()
            .filter { it.isNotEmpty() }
            .map { encodeImage -> imageUtil.decodeBase64(encodeImage) }

    fun createPlayersGame(gameMode: Int): Single<Intent> = IntentUtil.createPlayersGameIntent(repositoryManager!!.getPlayer(), gameMode, imageUtil, context)

    fun setImageDefaultPreUpdate(): Single<Boolean> = remoteDb.setImageDefaultPreUpdate()
            .flatMap { arrayFromBitmap ->
                var byteArray = arrayFromBitmap
                if (byteArray.isEmpty()) {

                    val bitmap = imageUtil.drawableToBitmap(checkersConfiguration.getDefaultAvatarDrawable()!!)
                    byteArray = imageUtil.createByteArrayFromBitmap(bitmap)
                }
                sharedPreferencesManager.setEncodeImageDefaultPreUpdate(imageUtil.encodeBase64Image(byteArray))
            }

    override fun setImageProfileTmp(imageProfileTmp: Bitmap?) {
        this.imageProfileTmpEncodeBase = imageUtil.encodeBase64Image(imageProfileTmp!!)
    }

    override fun getImageProfileTmp(): String? = this.imageProfileTmpEncodeBase

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