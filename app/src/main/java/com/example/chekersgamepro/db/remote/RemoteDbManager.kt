package com.example.chekersgamepro.db.remote

import android.util.Log
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.remote.firebase.FirebaseManager
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.models.player.data.IPlayer
import com.example.chekersgamepro.models.player.data.PlayerImpl
import com.example.chekersgamepro.models.player.data.RemotePlayerData
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import com.example.chekersgamepro.util.network.NetworkUtil
import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import kotlin.collections.HashMap

class RemoteDbManager(userProfile: UserProfileImpl?) : IRemoteDb {

    private val firebaseManager = FirebaseManager()

    private val remotePlayersCache = HashMap<Long, IPlayer>()

    private var userProfile: IUserProfile = UserProfileImpl()

    private var player: IPlayer = PlayerImpl()

    private var remotePlayerCacheTmpGuest: IPlayer = PlayerImpl()

    private var isYourTurn = false

    private val imageUtil = CheckersImageUtil.create()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create().applicationContext

//    private var isShowPlayersChanges = true

    init {
        if (userProfile != null) {
            this.userProfile = userProfile

            this.player.setPlayerName(userProfile.getUserName())
            this.player.setIsCanPlay(true)
            this.player.setPlayerId(userProfile.getUserId())
            this.player.setAvatarEncode(userProfile.getAvatarEncode())

            createDialogStateCreator()

        }
    }

    override fun isUserNameExistServer(userName: String): Single<Boolean> = firebaseManager.isUserNameExist(userName)

    override fun createUser(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<IUserProfile> {

        this.userProfile.setIsRegistered(true)
        this.userProfile.setUserName(userName)
        this.userProfile.setUserId(id)
        this.userProfile.setMoney(200)
        this.userProfile.setAvatarEncode(encodeImageDefaultPreUpdate)

        return firebaseManager.addNewUser(userProfile)
                .map { userProfile }
    }

    override fun createPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<Optional<IPlayer>> {
        this.player.setPlayerName(userName)
        this.player.setPlayerId(id)
        this.player.setIsCanPlay(true)
        this.player.setAvatarEncode(encodeImageDefaultPreUpdate)

        return createPlayer()

    }

    override fun createPlayer(): Single<Optional<IPlayer>> {
        return if (player.getPlayerName().isEmpty()) Single.just(Optional.absent())
        else firebaseManager.addNewPlayer(player)
                .doOnError {
                    Log.d("TEST_GAME", "RemoteDbManager -> createPlayer  doOnError")
                }
                .map { Optional.of(player) }

    }

    override fun setIsCanPlay(isCanPlay: Boolean): Completable {
        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.getPlayerName()}/canPlay"] = false

        return firebaseManager.setIsCanPlay(this.player.getLevelPlayer().toString(), fieldsMap)
                .doOnError {
                    Log.d("TEST_GAME", "RemoteDbManager -> after firebaseManager.setIsCanPlay( doOnError:  ${it.message} ")
                }
    }

    override fun getDataPlayerChanges(): Observable<IPlayer> =
            firebaseManager.getPlayerChanges(this.player.getPlayerName(), this.player.getLevelPlayer().toString())
                    .doOnNext { this.player = it }
                    .doOnNext {
                        it.getRequestOnlineGameStatus().ordinal

                    }

    override fun getAllAvailableOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>> {
        val list = ArrayList<IOnlinePlayerEvent>()

        return firebaseManager.getAllPlayersByLevel(this.player.getLevelPlayer())
//                .filter { isShowPlayersChanges }
//                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext { list.clear() }
                .flatMap {
                    Observable.fromIterable(it)
                            .map { dataSnapshot -> dataSnapshot.getValue(PlayerImpl::class.java) }
                            .cast(IPlayer::class.java)
                            .filter(this::filterRemotePlayer)
                            .cast(IPlayer::class.java)
                            .doOnNext { remotePlayer -> remotePlayersCache[remotePlayer.getPlayerId()] = remotePlayer }
                            .doOnNext { remotePlayer -> list.add(createOnlinePlayerEvent(remotePlayer)) }
                            .map { list }
                }
                .map { list }
    }

    private fun filterRemotePlayer(remotePlayer: IPlayer): Boolean {
        if (this.player.getPlayerName() == remotePlayer.getPlayerName()) return false
        return remotePlayer.isCanPlay() /*|| (!remotePlayer.isCanPlay() && remotePlayer.getRemotePlayer() == this.player.getPlayerName())*/
    }

    private fun createOnlinePlayerEvent(remotePlayer: IPlayer): IOnlinePlayerEvent =
            OnlinePlayerEventImpl(
                    remotePlayer.getPlayerName()
                    , remotePlayer.getLevelPlayer()
                    , remotePlayer.getPlayerId()
                    , imageUtil.decodeBase64Async(remotePlayer.getAvatarEncode())
                    , remotePlayer.getTotalWin()
                    , remotePlayer.getTotalLoss())

    override fun getRemotePlayerById(playerId: Long): IPlayer {
        return remotePlayersCache[playerId]!!
    }

    override fun getRemotePlayer() = this.remotePlayerCacheTmpGuest

    override fun sendRequestOnlineGame(idRemotePlayer: Long): Completable {

        if (!this.player.isCanPlay()) {
            return Completable.complete()
        }

        val fieldsMap = HashMap<String, Any>()

        // Find the player that the user want to be play with him(guest)
        this.remotePlayerCacheTmpGuest = remotePlayersCache[idRemotePlayer]!!

        // Set the guest player fields
        setSendRequestByPlayer(
                fieldsMap
                , createRemotePlayerDataByPlayer(this.player)//Owner
                , RequestOnlineGameStatus.RECEIVE_REQUEST
                , PlayersCode.PLAYER_ONE.ordinal
                , PlayersCode.PLAYER_ONE.ordinal
                , false
                , remotePlayerCacheTmpGuest.getPlayerName())

        // Set the owner player fields
        setSendRequestByPlayer(
                fieldsMap
                , createRemotePlayerDataByPlayer(remotePlayerCacheTmpGuest)//Guest
                , RequestOnlineGameStatus.SEND_REQUEST
                , PlayersCode.PLAYER_ONE.ordinal
                , PlayersCode.PLAYER_TWO.ordinal
                , true
                , this.player.getPlayerName())

        return firebaseManager
                .sendRequestOnlineGame(fieldsMap, this.player.getLevelPlayer().toString())
    }

    private fun createRemotePlayerDataByPlayer(player: IPlayer): RemotePlayerData {
        return RemotePlayerData(
                player.getPlayerId()
                , player.getAvatarEncode()
                , player.getPlayerName())
    }

    private fun setSendRequestByPlayer(fieldsMap: HashMap<String, Any>
                                       , remotePlayer: RemotePlayerData
                                       , requestOnlineGameStatus: RequestOnlineGameStatus
                                       , nowPlay: Int
                                       , playerCode: Int
                                       , isOwner: Boolean
                                       , playerName: String) {
        fieldsMap["/$playerName/remotePlayerActive"] = remotePlayer
        fieldsMap["/$playerName/canPlay"] = false
        fieldsMap["/$playerName/requestOnlineGameStatus"] = requestOnlineGameStatus
        fieldsMap["/$playerName/nowPlay"] = nowPlay
        fieldsMap["/$playerName/playerCode"] = playerCode
        fieldsMap["/$playerName/technicalLoss"] = false
        fieldsMap["/$playerName/owner"] = isOwner

    }

    private fun getPlayerIdByState(status: RequestOnlineGameStatus, remotePlayerId: Long): Long {
        return if ((status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.isOwner())
                || (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !player.isOwner())) {
            // The remote player this is the owner
            remotePlayerId
        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.isOwner()) {
            // The remote player this is the guest
            return this.remotePlayerCacheTmpGuest.getPlayerId()
        } else {
            -1L
        }
    }

    private fun getRemotePlayerByState(status: RequestOnlineGameStatus, playerId: Long): IOnlinePlayerEvent {
//        Log.d("TEST_GAME", "1 RemoteDbManager getRemotePlayerByState")
        return if ((status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.isOwner())
                || (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !player.isOwner())) {
            // The remote player this is the owner
            val remotePlayer = remotePlayersCache[playerId]
            val remotePlayerEvent = createOnlinePlayerEvent(remotePlayer!!)
            remotePlayerEvent

        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.isOwner()) {
            // The remote player this is the guest
            val remotePlayer = remotePlayersCache[playerId]
            val remotePlayerEvent = createOnlinePlayerEvent(remotePlayer!!)
            remotePlayerEvent
        } else {
            OnlinePlayerEventImpl()
        }

    }

//    private fun isNeedShowActionMessage(requestGame: RequestOnlineGameStatus): Boolean {
//        return requestGame.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !this.player.isOwner()
//    }
//
//    private fun isNeedShowMessage(requestGame: RequestOnlineGameStatus): Boolean {
//        val isOwner = this.player.isOwner()
//        return requestGame.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !isOwner
//                || requestGame.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && isOwner
//                || requestGame.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !isOwner
//    }

    private fun getMsgByState(status: RequestOnlineGameStatus): String {
        val isOwner = this.player.isOwner()

        return if (status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !isOwner) {
            // The remote player this is the owner
            context.getString(R.string.activity_home_page_online_players_dialog_request_game_text, player.getPlayerName())

        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && isOwner) {
            // The remote player this is the guest
            "No, next time"
        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !isOwner) {
            // The remote player this is the guest
            "Sorry, I'm giving up"
        } else {
            ""
        }
    }

    private var dialogStateCreator = BehaviorSubject.createDefault(DialogStateCreator())

    private fun createDialogStateCreator() {
        getDataPlayerChanges()
                .filter {
                    Log.d("TEST_GAME", "RemoteDbManager -> createDialogStateCreator-> filter-> STATE: ${it.getRequestOnlineGameStatus().name}")
                    it.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.EMPTY.ordinal
                            && it.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.SEND_REQUEST.ordinal
                            && it.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal
                }
                .map { player ->
                    Log.d("TEST_GAME", "RemoteDbManager -> createDialogStateCreator-> PASS filter-> STATE: ${player.getRequestOnlineGameStatus().name}")

                    val status = player.getRequestOnlineGameStatus()
                    val remotePlayerActive = player.getRemotePlayerActive()

                    val remotePlayerId = remotePlayerActive.remotePlayerId

                    val remotePlayerIdByState = getPlayerIdByState(status, remotePlayerId)
                    val remotePlayer = getRemotePlayerByState(status, remotePlayerIdByState)
                    val msgByState = getMsgByState(status)
                    return@map DialogStateCreator(remotePlayer, msgByState, status, this.player.isOwner())
                }
                .subscribe(this::setDialogCreator)
    }

    override fun setDialogCreator(dialogStateCreator: DialogStateCreator) {
        this.dialogStateCreator.onNext(dialogStateCreator)
    }

    override fun getDialogState(): Observable<DialogStateCreator> = dialogStateCreator.hide()

    override fun getRequestGameStatus(): Observable<RequestOnlineGameStatus> =
            firebaseManager.getRequestStatusChanges(this.player.getPlayerName(), this.player.getLevelPlayer().toString())

    override fun startOnlineGame(): Observable<Boolean> {
        return getRequestGameStatus()
                .map { status -> status.ordinal == RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal }
                .filter(Functions.equalsWith(true))
                .doOnNext { Log.d("TEST_GAME", "RemoteDbManager startOnlineGame -> PLAYER: ${player.toString()}") }
    }

    override fun finishRequestOnlineGame(): Completable {

        Log.d("TEST_GAME", "RemoteDbManager finishRequestOnlineGame -> PLAYER: $player")


        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.getPlayerName()}/canPlay"] = true
        fieldsMap["/${this.player.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.getPlayerName()}/remotePlayerActive"] = RemotePlayerData()
        fieldsMap["/${this.player.getPlayerName()}/owner"] = false

        return firebaseManager.finishRequestOnlineGame(fieldsMap, this.player.getLevelPlayer().toString())
    }

    override fun isStillSendRequest(): Observable<Boolean> =
            getRequestGameStatus()
                    .map { it.ordinal == RequestOnlineGameStatus.SEND_REQUEST.ordinal }

    override fun setDeclineRequestGameStatus(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager -> ignoredRequestGameStatusItself")

        val fieldsMap = HashMap<String, Any>()

        // The player is the owner(itself)
        fieldsMap["/${this.player.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_GUEST
        fieldsMap["/${this.player.getRemotePlayerActive().remotePlayerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_OWNER

       return firebaseManager
                .ignoredRequestGameStatusItself(fieldsMap, this.player.getLevelPlayer().toString())
    }

    override fun isRelevantRequestGame(): Single<Boolean> {
        Log.d("TEST_GAME", "111 RemoteDbManager isRelevantRequestGame")

        val player = this.player
        return firebaseManager
                .getRemotePlayerActiveByRemotePlayer(player.getRemotePlayerActive().remotePlayerName, player.getLevelPlayer().toString())
                .map { remotePlayerActiveByRemotePlayer ->
                    val result = this.player.getPlayerName() == remotePlayerActiveByRemotePlayer
                    Log.d("TEST_GAME", "111 RemoteDbManager isRelevantRequestGame RESULT: $result")
                    result
                }
    }

    override fun declineOnlineGame(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager declineOnlineGame")
        if (this.player.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal) return Completable.complete()

        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwner = this.player.getRemotePlayerActive().remotePlayerName

        val fieldsMap = HashMap<String, Any>()

        // The remote player is the owner
        // that need to notify on the decline game
        fieldsMap["/$remotePlayerNameOwner/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_GUEST

        fieldsMap["/${this.player.getPlayerName()}/canPlay"] = true
        fieldsMap["/${this.player.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.getPlayerName()}/remotePlayerActive"] = RemotePlayerData()

        return firebaseManager.declineOnlineGame(fieldsMap, this.player.getLevelPlayer().toString())
    }

    override fun cancelRequestGame(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager cancelRequestGame")

        if (this.player.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.SEND_REQUEST.ordinal) return Completable.complete()
        val fieldsMap = HashMap<String, Any>()
        fieldsMap["/${this.remotePlayerCacheTmpGuest.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_OWNER

        return firebaseManager.cancelRequestGame(fieldsMap, player.getLevelPlayer().toString())
                .andThen(finishRequestOnlineGame())

    }

    override fun acceptOnlineGame(): Completable {

        if (this.player.getRequestOnlineGameStatus().ordinal != RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal) return Completable.complete()

        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwnerFirst = this.player.getRemotePlayerActive().remotePlayerName

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/$remotePlayerNameOwnerFirst/requestOnlineGameStatus"] = RequestOnlineGameStatus.ACCEPT_BY_GUEST

        fieldsMap["/${player.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.ACCEPT_BY_GUEST

        return firebaseManager.acceptOnlineGame(player.getLevelPlayer().toString(), fieldsMap)
    }

    override fun notifyEndTurn(move: RemoteMove): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.getRemotePlayerActive().remotePlayerName}/remoteMove"] = move

        return firebaseManager.notifyMove(player.getLevelPlayer().toString(), fieldsMap)
    }

    override fun getRemoteMove(): Observable<RemoteMove> =
            firebaseManager.getRemoteMove(player.getPlayerName(), player.getLevelPlayer().toString())
                    .filter { it.idEndCell != -1 && it.idStartCell != -1 }


    override fun setFinishGameTechnicalLoss(): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.getPlayerName()}/technicalLoss"] = true

        return firebaseManager
                .pingFinishGameTechnicalLoss(player.getLevelPlayer().toString(), fieldsMap)
    }

    override fun isTechnicalWin(): Observable<Boolean> =
            firebaseManager.isTechnicalWin(this.player.getRemotePlayerActive().remotePlayerName, player.getLevelPlayer().toString())

    override fun setMoney(money: Int): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.userProfile.getUserName()}/money"] = money

        return firebaseManager.setMoney(fieldsMap)
    }

    override fun resetPlayer(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager resetPlayer")

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.getPlayerName()}/owner"] = false
        fieldsMap["/${this.player.getPlayerName()}/canPlay"] = true
        fieldsMap["/${this.player.getPlayerName()}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.getPlayerName()}/nowPlay"] = PlayersCode.EMPTY.ordinal
        fieldsMap["/${this.player.getPlayerName()}/playerCode"] = PlayersCode.EMPTY.ordinal
        fieldsMap["/${this.player.getPlayerName()}/technicalLoss"] = false
        fieldsMap["/${this.player.getPlayerName()}/remoteMove"] = RemoteMove()
        fieldsMap["/${this.player.getPlayerName()}/remotePlayerActive"] = RemotePlayerData()

        return firebaseManager.resetPlayer(this.player.getLevelPlayer().toString(), fieldsMap)
    }

    override fun setImageProfileAndPlayer(encodeImage: String, playerName: String): Completable =
            firebaseManager.setImageProfileAndPlayer(this.player.getPlayerName(), this.player.getLevelPlayer().toString(), encodeImage)

    override fun setImageDefaultPreUpdate(): Single<ByteArray?> {
        if (!NetworkUtil().isAvailableNetwork()) {
            return Single.just(ByteArray(0))
        }
        return firebaseManager.setImageDefaultPreUpdate()
                .onErrorReturnItem(ByteArray(0))
    }
}