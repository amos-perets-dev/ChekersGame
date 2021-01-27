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
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.menu.online.dialog.DialogStateCreator
import com.example.chekersgamepro.screens.registration.RegistrationStatus
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersConfiguration
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.menu.settings.SettingsData
import com.example.chekersgamepro.screens.homepage.menu.topplayers.model.ITopPlayer
import com.example.chekersgamepro.util.IntentUtil
import com.example.chekersgamepro.util.StringUtil
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


class RepositoryManager : Repository {

    private val context = CheckersApplication.create()

    private val checkersConfiguration = CheckersConfiguration.getInstance()

    private val imageUtil = CheckersImageUtil.create()

    private val sharedPreferencesManager = SharedPreferencesManager(context)

    private var userNameExistInvalid = HashSet<String>()

    private var userNameNotExistValid = HashSet<String>()

    private val realmManager = RealmManager()

    private val userProfile = realmManager.getDefaultRealm().where(UserProfileImpl::class.java).findFirst()

    private val remoteDb: IRemoteDb = RemoteDbManager(userProfile)

    private val userProfileManager = UserProfileManager(realmManager, remoteDb, realmManager.getUserProfileDataChanges())

    private val playerManager = PlayerManager(realmManager, remoteDb)

    private var imageProfileTmpEncodeBase: String? = null

    private val availableOnlinePlayersList = BehaviorSubject.create<List<IOnlinePlayerEvent>>()
    private val topPlayersList = BehaviorSubject.create<List<ITopPlayer>>()

    init {
        Log.d("TEST_GAME", "RepositoryManager init: $this")

        remoteDb.getAllAvailableOnlinePlayersByLevel()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Log.d("TEST_GAME", "RepositoryManager getAllAvailableOnlinePlayersByLevel")
                    availableOnlinePlayersList.onNext(it)
                }

        remoteDb.getTopPlayersListByMoney()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    Log.d("TEST_GAME", "RepositoryManager getTopPlayersListByMoney")
                    topPlayersList.onNext(it)
                }
    }

    fun createDialogStateCreator() = remoteDb.createDialogStateCreator()

    override fun getTopPlayersList(): Observable<List<ITopPlayer>> =
            this.topPlayersList.hide()
                    .subscribeOn(Schedulers.io())

    fun isRegistered(): Boolean = sharedPreferencesManager.isRegistered()

    fun setRunFirstTime() {
        sharedPreferencesManager.setRunFirstTime()
    }

    override fun addNewSettings(settingsData: SettingsData) = realmManager.insertAsync(settingsData)


    override fun getSettingsData(): Flowable<SettingsData> = realmManager.getSettingsData()

    private fun getEncodeImageDefaultPreUpdate(): String? = sharedPreferencesManager.getEncodeImageDefaultPreUpdate()

    override fun addNewUser(userName: String): Single<RegistrationStatus> =
            isUserNameExistServer(userName)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap { isExist ->
                        if (isExist) Single.just(RegistrationStatus.NOT_AVAILABLE)

                        val id = StringUtil.convertToAscii(userName) + System.currentTimeMillis()

                        return@flatMap userProfileManager.createUser(id, userName, getEncodeImageDefaultPreUpdate().toString())
                                .andThen(playerManager.createPlayer(id, userName, getEncodeImageDefaultPreUpdate().toString())
                                        .andThen(playerManager.createTopPlayer(id, userName, getEncodeImageDefaultPreUpdate().toString())))
                                .doOnError { Log.d("TEST_GAME", "22 doOnError addNewUser: ${it.message}") }
                                .toSingleDefault(RegistrationStatus.REGISTERED)
                                .onErrorReturnItem(RegistrationStatus.ERROR)
                                .doOnEvent { status, throwable ->
                                    if (status.ordinal == RegistrationStatus.REGISTERED.ordinal) {
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

    fun getOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>> = availableOnlinePlayersList.hide()

    private fun isStillSendRequestStatus(): Observable<Boolean> {
        return Observable.timer(15000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .flatMap { isStillSendRequest() }
    }

    private fun isRelevantRequestGame(): Observable<Boolean> {

        return playerManager.isOwnerPlayerAsync()
                .filter(Functions.equalsWith(true))
                .doOnNext { isOwnerPlayerAsync ->
                    Log.d("TEST_GAME", "!@#$%^&*(*&^%$#@ isOwnerPlayerAsync: $isOwnerPlayerAsync")
                }
                .delay(3000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnNext { Log.d("TEST_GAME", "3333 RepositoryManager -> isRelevantRequestGame") }
                .flatMapSingle {
                    Log.d("TEST_GAME", "4444 RepositoryManager -> isRelevantRequestGame")
                    return@flatMapSingle remoteDb.isRelevantRequestGame()
                            .doOnEvent { t1, t2 ->
                                Log.d("TEST_GAME", "3 RepositoryManager -> doOnNext checkRequestGame RESULT: $t1")
                            }
                }
    }

    fun isStillRelevantRequestGame(): Completable {
        Log.d("TEST_GAME", "1111 RepositoryManager -> isStillRelevantRequestGame")
        return isRelevantRequestGame()
                .flatMapCompletable { isRelevantRequestGame ->
                    if (isRelevantRequestGame) {
                        Log.d("TEST_GAME", "RepositoryManager -> flatMapCompletable -> isStillRelevantRequestGame  if (isRelevantRequestGame)")
                        return@flatMapCompletable isStillSendRequestStatus()
                                .filter(Functions.equalsWith(true))
                                .flatMapCompletable { remoteDb.setDeclineRequestGameStatus() }
                    } else {
                        Log.d("TEST_GAME", "RepositoryManager -> flatMapCompletable -> isStillRelevantRequestGame  } else {")
                        return@flatMapCompletable remoteDb.setDeclineRequestGameStatus()
                    }
                }
    }

    fun sendRequestOnlineGame(remotePlayerId: Long): Completable =
            Observable.timer(200, TimeUnit.MILLISECONDS)
                    .flatMapCompletable { playerManager.sendRequestOnlineGame(remotePlayerId) }

    fun declineOnlineGame(): Completable = playerManager.declineOnlineGame()

    fun acceptOnlineGame(): Completable = playerManager.acceptOnlineGame()

    fun getDialogState(): Observable<DialogStateCreator> = remoteDb.getDialogState()

    fun setDialogCreatorByOwner(dialogStateCreator: DialogStateCreator) {
        remoteDb.setDialogCreator(dialogStateCreator)
    }

    fun isStillSendRequest(): Observable<Boolean> = remoteDb.isStillSendRequest()

    override fun getRemotePlayerById(playerId: Long) = remoteDb.getRemotePlayerById(playerId)

    fun getRequestGameStatus() : Observable<RequestOnlineGameStatus> = remoteDb.getRequestGameStatus()

    override fun technicalFinishGamePlayer() = resetPlayer()

    /**
     * If the oner get the notify on the decline request game, he needed to clean the request
     */
    fun finishRequestOnlineGame(): Completable =
            Observable.timer(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .flatMapCompletable { remoteDb.finishRequestOnlineGame() }

    fun isOwnerPlayerAsync() = playerManager.isOwnerPlayerAsync()

    fun startOnlineGame(): Observable<Boolean> = remoteDb.startOnlineGame()

    fun getRemoteMove(): Observable<RemoteMove> = playerManager.getRemoteMove()

    fun getNowPlayAsync(): Observable<Int> = playerManager.getNowPlayAsync()

    fun notifyEndTurn(move: RemoteMove): Completable = remoteDb.notifyEndTurn(move)

    fun setMoney(isWinAndNeedUpdate: Boolean? = null): Completable = userProfileManager.setUserDataTmp(isWinAndNeedUpdate)

    fun setFinishGameTechnicalLoss(): Completable = remoteDb.setFinishGameTechnicalLoss()

    fun isTechnicalWin(): Observable<Boolean> = remoteDb.isTechnicalWin()

    fun resetPlayer(): Completable = playerManager.resetPlayer()

    fun startGetDataPlayerChanges() {
        playerManager.startGetDataPlayerChanges()
    }

    fun isDefaultImage() = sharedPreferencesManager.isDefaultImage()

    override fun storeImage(): Completable {
        return userProfileManager.setEncodeImageProfile(getImageProfileTmp())
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(sharedPreferencesManager.setIsDefaultImage())
    }

    fun getUserProfileTotalWinChanges(): Flowable<String> = userProfileManager.getUserProfileTotalWinChanges()

    fun getUserProfileTotalLossChanges(): Flowable<String> = userProfileManager.getUserProfileTotalLossChanges()

    fun getUserProfileMoneyChanges(): Flowable<String> = userProfileManager.getUserProfileMoneyChanges()

    fun getUserProfileLevelChanges(): Flowable<String> = userProfileManager.getUserProfileLevelChanges()
    fun getUserProfileName(): Flowable<String> = userProfileManager.getUserProfileName()


    fun getImageProfileChanges(): Flowable<Bitmap?> =
            this.userProfileManager.getEncodeImageProfileChanges()
                    .doOnNext { Log.d("TEST_GAME", "1212 userProfileManager.getImageProfileAsync()") }
                    .filter { it.isNotEmpty() }
                    .map { encodeImage -> imageUtil.decodeBase64(encodeImage) }
                    .doOnNext { Log.d("TEST_GAME", "5555 userProfileManager.getImageProfileAsync()") }

    fun createPlayersGame(gameMode: Int): Single<Intent> =
            IntentUtil.createPlayersGameIntent(playerManager.getPlayerAsync(), gameMode, imageUtil, context)

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

    fun cancelRequestGame(): Completable = this.remoteDb.cancelRequestGame()

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