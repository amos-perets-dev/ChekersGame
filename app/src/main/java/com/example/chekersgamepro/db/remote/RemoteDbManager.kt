package com.example.chekersgamepro.db.remote

import android.util.Log
import com.example.chekersgamepro.R
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.checkers.CheckersImageUtil
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.remote.firebase.FirebaseManager
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.models.player.data.PlayerData
import com.example.chekersgamepro.models.player.data.RemotePlayerData
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.online.dialog.DialogStateCreator
import com.example.chekersgamepro.screens.homepage.topplayers.TopPlayerData
import com.example.chekersgamepro.screens.homepage.topplayers.model.ITopPlayer
import com.example.chekersgamepro.screens.homepage.topplayers.model.TopPlayerImpl
import com.example.chekersgamepro.util.network.NetworkUtil
import com.google.common.base.Optional
import com.google.common.collect.Lists
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.functions.Functions
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class RemoteDbManager(userProfile: UserProfileImpl?) : IRemoteDb {

    private val firebaseManager = FirebaseManager()

    private val remotePlayersCache = HashMap<Long, PlayerData>()

    private var userProfile: IUserProfile = UserProfileImpl()

    private var player = PlayerData()

    private lateinit var topPlayer: TopPlayerData

    private var remotePlayerCacheTmpGuest = PlayerData()

    private val imageUtil = CheckersImageUtil.create()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create().applicationContext

//    private var isShowPlayersChanges = true

    companion object {
        const val TOP_PLAYERS_LIMIT = 5
    }

    init {
        if (userProfile != null) {
            this.userProfile = userProfile

            this.player.playerName = userProfile.getUserName()
            this.player.canPlay = true
            this.player.id = userProfile.getUserId()
            this.player.avatarEncodeImage = userProfile.getAvatarEncodeImage()
            this.player.totalWin = userProfile.getTotalWin()
            this.player.totalLoss = userProfile.getTotalLoss()

            this.topPlayer = TopPlayerData(
                    userProfile.getUserName(),
                    userProfile.getTotalWin(),
                    userProfile.getTotalLoss(),
                    userProfile.getMoney(),
                    userProfile.getAvatarEncodeImage())
        }
    }

    override fun isUserNameExistServer(userName: String): Single<Boolean> = firebaseManager.isUserNameExist(userName)

    override fun createUser(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<IUserProfile> {

        this.userProfile.setIsRegistered(true)
        this.userProfile.setUserName(userName)
        this.userProfile.setUserId(id)
        this.userProfile.setMoney(200)
        this.userProfile.setAvatarEncodeImage(encodeImageDefaultPreUpdate)
        this.userProfile.setTotalLoss(0)
        this.userProfile.setTotalWin(0)
        this.userProfile.setLevelUser(1)

        return firebaseManager.addNewUser(userProfile)
                .map { userProfile }
    }

    override fun createPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<Optional<PlayerData>> {
        this.player.playerName = userName
        this.player.id = id
        this.player.canPlay = true
        this.player.avatarEncodeImage = encodeImageDefaultPreUpdate

        return createPlayer()
    }

    override fun createTopPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable {

        this.topPlayer = TopPlayerData(userName, 0, 0, 200, encodeImageDefaultPreUpdate)
        return firebaseManager.addNewTopPlayer(this.topPlayer).ignoreElement()
    }

    override fun createPlayer(): Single<Optional<PlayerData>> {
        return if (this.player.playerName.isEmpty()) Single.just(Optional.absent())
        else firebaseManager.addNewPlayer(this.player)
                .doOnError {
                    Log.d("TEST_GAME", "RemoteDbManager -> createPlayer  doOnError")
                }
                .map { Optional.of(player) }

    }

    override fun setIsCanPlay(isCanPlay: Boolean): Completable {
        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.playerName}/canPlay"] = false

        return firebaseManager.setIsCanPlay(this.player.userLevel.toString(), fieldsMap)
                .doOnError {
                    Log.d("TEST_GAME", "RemoteDbManager -> after firebaseManager.setIsCanPlay( doOnError:  ${it.message} ")
                }
    }

    override fun getDataPlayerChanges(): Observable<PlayerData> =
            firebaseManager.getPlayerChanges(this.player.playerName, this.player.userLevel.toString())
                    .doOnNext { this.player = it }


    private var minValueStopCheck = -1

    override fun getTopPlayersListByMoney(): Observable<List<ITopPlayer>> {
        val topPlayersList = ArrayList<ITopPlayer>()
        val topPlayersMap = HashMap<Int, ArrayList<ITopPlayer>>()

        return firebaseManager.getTopPlayersListChanges()
                .subscribeOn(Schedulers.io())
                .doOnNext { topPlayersMap.clear() }
                .doOnNext { topPlayersList.clear() }
                .flatMap { list ->

                    for (value in list) {

                        val player = value.getValue(TopPlayerData::class.java)!!
                        val money = player.money

                        val listFromMap = topPlayersMap[money]
                        if (listFromMap == null && topPlayersMap.size < TOP_PLAYERS_LIMIT) {

                            topPlayersMap[money] = Lists.newArrayList(createTopPlayerModel(topPlayersMap.size + 1, true, player))

                            if (topPlayersMap.size == TOP_PLAYERS_LIMIT) {
                                this.minValueStopCheck = money
                            }
                        } else if (listFromMap != null) {

                            listFromMap.add(createTopPlayerModel(-1, false, player))
                        }

                        if (money != this.minValueStopCheck && topPlayersMap.size == TOP_PLAYERS_LIMIT) {
                            break
                        }
                    }

                    val sortedMap = topPlayersMap.toSortedMap(reverseOrder())

                    sortedMap.values.forEach {
                        topPlayersList.addAll(it)
                    }

                    Observable.just(topPlayersList)
                }
    }

    private fun createTopPlayerModel(position: Int, isNumber: Boolean, player: TopPlayerData): ITopPlayer {
        return TopPlayerImpl(
                player.playerName,
                player.totalWin,
                player.totalLoss,
                player.money,
                imageUtil.decodeBase64Async(player.avatarEncodeImage),
                isNumber,
                position)
    }

    override fun getAllAvailableOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>> {
        val list = ArrayList<IOnlinePlayerEvent>()

        return this.firebaseManager.getAllPlayersByLevel(this.player.userLevel)
//                .filter { isShowPlayersChanges }
//                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext { list.clear() }
                .flatMap {
                    Observable.fromIterable(it)
                            .map { dataSnapshot -> dataSnapshot.getValue(PlayerData::class.java)!! }
                            .filter(this::filterRemotePlayer)
                            .doOnNext { remotePlayer -> this.remotePlayersCache[remotePlayer.id] = remotePlayer }
                            .doOnNext { remotePlayer -> list.add(createOnlinePlayerEvent(remotePlayer)) }
                            .map { list }
                }
                .map { list }
    }

    private fun filterRemotePlayer(remotePlayer: PlayerData): Boolean {
        if (this.player.playerName == remotePlayer.playerName) return false
        return remotePlayer.canPlay /*|| (!remotePlayer.isCanPlay() && remotePlayer.getRemotePlayer() == this.player.getPlayerName())*/
    }

    private fun createOnlinePlayerEvent(remotePlayer: PlayerData): IOnlinePlayerEvent =
            OnlinePlayerEventImpl(
                    remotePlayer.playerName
                    , remotePlayer.userLevel
                    , remotePlayer.id
                    , imageUtil.decodeBase64Async(remotePlayer.avatarEncodeImage)
                    , remotePlayer.totalWin
                    , remotePlayer.totalLoss)

    override fun getRemotePlayerById(playerId: Long): PlayerData {
        return remotePlayersCache[playerId]!!
    }

    override fun sendRequestOnlineGame(idRemotePlayer: Long): Completable {

        if (!this.player.canPlay) {
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
                , remotePlayerCacheTmpGuest.playerName)

        // Set the owner player fields
        setSendRequestByPlayer(
                fieldsMap
                , createRemotePlayerDataByPlayer(remotePlayerCacheTmpGuest)//Guest
                , RequestOnlineGameStatus.SEND_REQUEST
                , PlayersCode.PLAYER_ONE.ordinal
                , PlayersCode.PLAYER_TWO.ordinal
                , true
                , this.player.playerName)

        return firebaseManager
                .sendRequestOnlineGame(fieldsMap, this.player.userLevel.toString())
    }

    private fun createRemotePlayerDataByPlayer(player: PlayerData): RemotePlayerData {
        return RemotePlayerData(
                player.id
                , player.avatarEncodeImage
                , player.playerName)
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
        return if ((status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.owner)
                || (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !player.owner)) {
            // The remote player this is the owner
            remotePlayerId
        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.owner) {
            // The remote player this is the guest
            return this.remotePlayerCacheTmpGuest.id
        } else {
            -1L
        }
    }

    private fun getRemotePlayerByState(status: RequestOnlineGameStatus, playerId: Long): IOnlinePlayerEvent {
//        Log.d("TEST_GAME", "1 RemoteDbManager getRemotePlayerByState")
        return if ((status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.owner)
                || (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_OWNER.ordinal && !player.owner)) {
            // The remote player this is the owner
            val remotePlayer = remotePlayersCache[playerId]
            val remotePlayerEvent = createOnlinePlayerEvent(remotePlayer!!)
            remotePlayerEvent

        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.owner) {
            // The remote player this is the guest
            val remotePlayer = remotePlayersCache[playerId]
            val remotePlayerEvent = createOnlinePlayerEvent(remotePlayer!!)
            remotePlayerEvent
        } else {
            OnlinePlayerEventImpl()
        }

    }

    private fun getMsgByState(status: RequestOnlineGameStatus): String {
        val isOwner = this.player.owner

        return if (status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !isOwner) {
            // The remote player this is the owner
            context.getString(R.string.activity_home_page_online_players_dialog_request_game_text, player.playerName)

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

    private var dialogStateCreator = BehaviorSubject.create<DialogStateCreator>()

    override fun createDialogStateCreator(): Disposable {
        return getDataPlayerChanges()
                .doOnNext {
                    Log.d("TEST_GAME", "1 RemoteDbManager -> createDialogStateCreator -> doOnNext")
                }
                .filter {
                    Log.d("TEST_GAME", "2 RemoteDbManager -> createDialogStateCreator -> filter")
                    it.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.EMPTY.ordinal
                            && it.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.SEND_REQUEST.ordinal
                            && it.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal
                }
                .doOnNext {
                    Log.d("TEST_GAME", "1 RemoteDbManager -> createDialogStateCreator -> after doOnNext")
                }
                .map { player ->
                    Log.d("TEST_GAME", "RemoteDbManager -> createDialogStateCreator-> PASS filter-> STATE: ${player.requestOnlineGameStatus}")

                    val status = player.requestOnlineGameStatus
                    val remotePlayerActive = player.remotePlayerActive

                    val remotePlayerId = remotePlayerActive.remotePlayerId

                    val remotePlayerIdByState = getPlayerIdByState(status, remotePlayerId)
                    val remotePlayer = getRemotePlayerByState(status, remotePlayerIdByState)
                    val msgByState = getMsgByState(status)
                    return@map DialogStateCreator(remotePlayer, msgByState, status, this.player.owner)
                }
                .subscribe(this::setDialogCreator)
    }

    override fun setDialogCreator(dialogStateCreator: DialogStateCreator) {
        Log.d("TEST_GAME", "RemoteDbManager -> setDialogCreator-> dialogStateCreator.onNext(dialogStateCreator)")
        this.dialogStateCreator.onNext(dialogStateCreator)
    }

    override fun getDialogState(): Observable<DialogStateCreator> = dialogStateCreator.hide()

    override fun getRequestGameStatus(): Observable<RequestOnlineGameStatus> =
            firebaseManager.getRequestStatusChanges(this.player.playerName, this.player.userLevel.toString())

    override fun startOnlineGame(): Observable<Boolean> {
        return getRequestGameStatus()
                .map { status -> status.ordinal == RequestOnlineGameStatus.ACCEPT_BY_GUEST.ordinal }
                .filter(Functions.equalsWith(true))
                .doOnNext { Log.d("TEST_GAME", "RemoteDbManager startOnlineGame -> PLAYER: ${player.toString()}") }
    }

    override fun finishRequestOnlineGame(): Completable {

        Log.d("TEST_GAME", "RemoteDbManager finishRequestOnlineGame -> PLAYER: $player")


        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.playerName}/canPlay"] = true
        fieldsMap["/${this.player.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.playerName}/remotePlayerActive"] = RemotePlayerData()
        fieldsMap["/${this.player.playerName}/owner"] = false

        return firebaseManager.finishRequestOnlineGame(fieldsMap, this.player.userLevel.toString())
    }

    override fun isStillSendRequest(): Observable<Boolean> =
            getRequestGameStatus()
                    .map { it.ordinal == RequestOnlineGameStatus.SEND_REQUEST.ordinal }

    override fun setDeclineRequestGameStatus(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager -> ignoredRequestGameStatusItself")

        val fieldsMap = HashMap<String, Any>()

        // The player is the owner(itself)
        fieldsMap["/${this.player.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_GUEST
        fieldsMap["/${this.player.remotePlayerActive.remotePlayerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_OWNER

        return firebaseManager
                .ignoredRequestGameStatusItself(fieldsMap, this.player.userLevel.toString())
    }

    override fun isRelevantRequestGame(): Single<Boolean> {
        Log.d("TEST_GAME", "111 RemoteDbManager isRelevantRequestGame")

        val player = this.player
        return firebaseManager
                .getRemotePlayerActiveByRemotePlayer(player.remotePlayerActive.remotePlayerName, player.userLevel.toString())
                .map { remotePlayerActiveByRemotePlayer ->
                    val result = this.player.playerName == remotePlayerActiveByRemotePlayer
                    Log.d("TEST_GAME", "111 RemoteDbManager isRelevantRequestGame RESULT: $result")
                    result
                }
    }

    override fun declineOnlineGame(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager declineOnlineGame")
        if (this.player.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal) return Completable.complete()

        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwner = this.player.remotePlayerActive.remotePlayerName

        val fieldsMap = HashMap<String, Any>()

        // The remote player is the owner
        // that need to notify on the decline game
        fieldsMap["/$remotePlayerNameOwner/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_GUEST

        fieldsMap["/${this.player.playerName}/canPlay"] = true
        fieldsMap["/${this.player.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.playerName}/remotePlayerActive"] = RemotePlayerData()

        return firebaseManager.declineOnlineGame(fieldsMap, this.player.userLevel.toString())
    }

    override fun cancelRequestGame(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager cancelRequestGame")

        if (this.player.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.SEND_REQUEST.ordinal) return Completable.complete()
        val fieldsMap = HashMap<String, Any>()
        fieldsMap["/${this.remotePlayerCacheTmpGuest.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.DECLINE_BY_OWNER

        return firebaseManager.cancelRequestGame(fieldsMap, player.userLevel.toString())
                .andThen(finishRequestOnlineGame())

    }

    override fun acceptOnlineGame(): Completable {

        if (this.player.requestOnlineGameStatus.ordinal != RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal) return Completable.complete()

        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwnerFirst = this.player.remotePlayerActive.remotePlayerName

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/$remotePlayerNameOwnerFirst/requestOnlineGameStatus"] = RequestOnlineGameStatus.ACCEPT_BY_GUEST

        fieldsMap["/${player.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.ACCEPT_BY_GUEST

        return firebaseManager.acceptOnlineGame(player.userLevel.toString(), fieldsMap)
    }

    override fun notifyEndTurn(move: RemoteMove): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.remotePlayerActive.remotePlayerName}/remoteMove"] = move

        return firebaseManager.notifyMove(player.userLevel.toString(), fieldsMap)
    }

    override fun getRemoteMove(): Observable<RemoteMove> =
            firebaseManager.getRemoteMove(player.playerName, player.userLevel.toString())
                    .filter { it.idEndCell != -1 && it.idStartCell != -1 }


    override fun setFinishGameTechnicalLoss(): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.playerName}/technicalLoss"] = true

        return firebaseManager
                .pingFinishGameTechnicalLoss(player.userLevel.toString(), fieldsMap)
    }

    override fun isTechnicalWin(): Observable<Boolean> =
            firebaseManager.isTechnicalWin(this.player.remotePlayerActive.remotePlayerName, player.userLevel.toString())

    override fun setMoney(money: Int): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.userProfile.getUserName()}/money"] = money

        return firebaseManager.setMoney(fieldsMap)
    }

    override fun setTotalGamesUserAndPlayer(totalLoss: Int, totalWin: Int): Completable {

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.userProfile.getUserName()}/totalLoss"] = totalLoss
        fieldsMap["/${this.userProfile.getUserName()}/totalWin"] = totalWin

        return firebaseManager.setTotalGames(fieldsMap, this.userProfile.getLevelUser().toString())
    }

    override fun setTopPlayer(totalWin: Int, totalLoss: Int, money: Int): Completable {
        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.userProfile.getUserName()}/totalLoss"] = totalLoss
        fieldsMap["/${this.userProfile.getUserName()}/totalWin"] = totalWin
        fieldsMap["/${this.userProfile.getUserName()}/money"] = money

        return firebaseManager.setTopPlayer(fieldsMap, this.userProfile.getUserName())
    }

    override fun resetPlayer(): Completable {
        Log.d("TEST_GAME", "RemoteDbManager resetPlayer")

        val fieldsMap = HashMap<String, Any>()

        fieldsMap["/${this.player.playerName}/owner"] = false
        fieldsMap["/${this.player.playerName}/canPlay"] = true
        fieldsMap["/${this.player.playerName}/requestOnlineGameStatus"] = RequestOnlineGameStatus.EMPTY
        fieldsMap["/${this.player.playerName}/nowPlay"] = PlayersCode.EMPTY.ordinal
        fieldsMap["/${this.player.playerName}/playerCode"] = PlayersCode.EMPTY.ordinal
        fieldsMap["/${this.player.playerName}/technicalLoss"] = false
        fieldsMap["/${this.player.playerName}/remoteMove"] = RemoteMove()
        fieldsMap["/${this.player.playerName}/remotePlayerActive"] = RemotePlayerData()

        return firebaseManager.resetPlayer(this.player.userLevel.toString(), fieldsMap)
    }

    override fun setImageProfileAndPlayer(encodeImage: String): Completable =
            firebaseManager.setImageProfileAndPlayer(this.player.playerName, this.player.userLevel.toString(), encodeImage)

    override fun setImageDefaultPreUpdate(): Single<ByteArray?> {
        if (!NetworkUtil().isAvailableNetwork()) {
            return Single.just(ByteArray(0))
        }
        return firebaseManager.setImageDefaultPreUpdate()
                .onErrorReturnItem(ByteArray(0))
    }
}