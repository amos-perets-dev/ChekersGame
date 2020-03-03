package com.example.chekersgamepro.db.remote

import android.util.Log
import com.example.chekersgamepro.data.data_game.DataGame
import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.db.remote.firebase.FirebaseManager
import com.example.chekersgamepro.enumber.PlayersCode
import com.example.chekersgamepro.models.player.IPlayer
import com.example.chekersgamepro.models.player.PlayerImpl
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.player.online.OnlinePlayerEventImpl
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.models.user.UserProfileImpl
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.Functions
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class RemoteDbManager : IRemoteDb {

    private val firebaseManager = FirebaseManager()

    private val remotePlayersCache = HashMap<Long, IPlayer>()

    private var userProfile: IUserProfile = UserProfileImpl()

    private var player: IPlayer = PlayerImpl()

    private var remotePlayerCacheTmp: IPlayer = PlayerImpl()

    private var isYourTurn = false

    override fun isUserNameExistServer(userName: String): Single<Boolean> = firebaseManager.isUserNameExist(userName)

    override fun createUser(id: Long, userName: String): Single<IUserProfile> {

        userProfile.setIsRegistered(true)
        userProfile.setUserName(userName)
        userProfile.setUserId(id)
        userProfile.setMoney(200)

        return firebaseManager.addNewUser(userProfile)
                .map { userProfile }
    }

    override fun createPlayer(id: Long, userName: String): Single<IPlayer> {
        player.setPlayerName(userName)
        player.setPlayerId(id)

        return firebaseManager.addNewPlayer(player)
                .map { player }

    }

    override fun setIsCanPlay(isCanPlay: Boolean): Completable =
            firebaseManager.setIsCanPlay(this.player.getPlayerName(), this.player.getLevelPlayer().toString(), isCanPlay)


    override fun getDataPlayerChanges(): Observable<IPlayer> =
            firebaseManager.getPlayerChanges(this.player.getPlayerName(), this.player.getLevelPlayer().toString())
                    .doOnNext { this.player = it }
                    .doOnNext { isYourTurn = this.player.getNowPlay() == this.player.getPlayerCode() }

    override fun getAllAvailableOnlinePlayersByLevel(playerName: String, level: Int): Observable<List<IOnlinePlayerEvent>> {
        val list = ArrayList<IOnlinePlayerEvent>()

        return firebaseManager.getAllPlayersByLevel(level)
                .doOnNext { list.clear() }
                .flatMap {
                    Observable.fromIterable(it)
                            .map { dataSnapshot -> dataSnapshot.getValue(PlayerImpl::class.java) }
                            .filter { remotePlayer -> (playerName != remotePlayer.getPlayerName()) && remotePlayer.isCanPlay() }
                            .cast(IPlayer::class.java)
                            .doOnNext { remotePlayer -> remotePlayersCache[remotePlayer.getPlayerId()] = remotePlayer }
                            .doOnNext { remotePlayer -> list.add(OnlinePlayerEventImpl(remotePlayer.getPlayerName(), remotePlayer.getLevelPlayer(), remotePlayer.getPlayerId())) }
                            .map { list }
                }
                .map { list }
    }

    override fun sendRequestOnlineGame(idRemotePlayer: Long): Completable {

        // Find the player that the user want to be play with him
        this.remotePlayerCacheTmp = remotePlayersCache[idRemotePlayer]!!

        this.remotePlayerCacheTmp.setRemotePlayer(player.getPlayerName())
        this.remotePlayerCacheTmp.setIsCanPlayer(false)
        this.remotePlayerCacheTmp.setRequestOnlineGameStatus(RequestOnlineGameStatus.RECEIVE_REQUEST)
        this.remotePlayerCacheTmp.setNowPlay(PlayersCode.PLAYER_ONE.ordinal)
        this.remotePlayerCacheTmp.setPlayerCode(PlayersCode.PLAYER_ONE.ordinal)
        this.remotePlayerCacheTmp.setIsTechnicalLoss(false)

        this.player.setIsOwner(true)
        this.player.setRemotePlayer(remotePlayerCacheTmp.getPlayerName())
        this.player.setIsCanPlayer(false)
        this.player.setRequestOnlineGameStatus(RequestOnlineGameStatus.SEND_REQUEST)
        this.player.setNowPlay(PlayersCode.PLAYER_ONE.ordinal)
        this.player.setPlayerCode(PlayersCode.PLAYER_TWO.ordinal)
        this.player.setIsTechnicalLoss(false)

        isYourTurn = true

        return firebaseManager.sendRequestOnlineGame(remotePlayerCacheTmp, this.player)

    }

    override fun getRequestGameMsgText(): Observable<String> =
            getDataPlayerChanges()
                    .map(IPlayer::getRequestOnlineGameStatus)
                    .map { status ->
                        return@map if (status.ordinal == RequestOnlineGameStatus.RECEIVE_REQUEST.ordinal && !player.isOwner()) {
                            this.player.getRemotePlayer() + " want to play"
                        } else if (status.ordinal == RequestOnlineGameStatus.DECLINE_BY_GUEST.ordinal && player.isOwner()) {
                            this.remotePlayerCacheTmp.getPlayerName() + " decline your request"
                        } else {
                            ""
                        }
                    }

    override fun getRequestGameStatus(): Observable<RequestOnlineGameStatus> =
            firebaseManager.getRequestStatusChanges(this.player.getPlayerName(), this.player.getLevelPlayer().toString())

    override fun finishRequestOnlineGame(): Completable {
        this.player.setIsOwner(false)
        this.player.setIsCanPlayer(true)
        this.player.setRemotePlayer("")
        this.player.setRequestOnlineGameStatus(RequestOnlineGameStatus.EMPTY)
        return firebaseManager.setDataPlayer(player, false)
                .filter(Functions.equalsWith(true))
                .ignoreElement()
    }

    override fun declineOnlineGame(): Completable {

        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwner = this.player.getRemotePlayer()

        this.player.setRemotePlayer("")
        this.player.setIsCanPlayer(true)
        this.player.setRequestOnlineGameStatus(RequestOnlineGameStatus.EMPTY)

        return firebaseManager.declineOnlineGame(remotePlayerNameOwner, player)
    }

    override fun acceptOnlineGame(): Completable {
        // The remote player is the owner
        // that need to notify on the decline game
        val remotePlayerNameOwner = this.player.getRemotePlayer()

        return firebaseManager.acceptOnlineGame(remotePlayerNameOwner, player.getPlayerName(), player.getLevelPlayer().toString())
    }

    override fun setNowPlayer(): Completable =
            firebaseManager.setNowPlayer(player.isOwner(), player.getPlayerName(), player.getRemotePlayer(), player.getLevelPlayer().toString())

    override fun getNowPlayer(): Observable<Int> = firebaseManager.getNowPlayer(player.getPlayerName(), player.getLevelPlayer().toString())

    override fun notifyEndTurn(move: RemoteMove) =
            firebaseManager.notifyMove(move, player.getRemotePlayer(), player.getLevelPlayer().toString())
                    .doOnEvent { isYourTurn = !isYourTurn }

    override fun getRemoteMove(): Observable<RemoteMove> =
            firebaseManager.getRemoteMove(player.getPlayerName(), player.getLevelPlayer().toString())
                    .delay(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .filter { it.idEndCell != -1 && it.idStartCell != -1 }


    override fun setFinishGameTechnicalLoss(): Completable =
            firebaseManager
                    .pingFinishGameTechnicalLoss(player.getPlayerName(), player.getLevelPlayer().toString()/*, true*/)

    override fun isTechnicalWin(): Observable<Boolean> =
            firebaseManager.isTechnicalWin(player.getRemotePlayer(), player.getLevelPlayer().toString())

    override fun setMoney(money: Int): Completable {
        return firebaseManager.setMoney(userProfile.getUserName(), money)
    }

    override fun resetPlayer() : Completable {
        this.player.setIsOwner(false)
        this.player.setRemotePlayer("")
        this.player.setIsCanPlayer(true)
        this.player.setRequestOnlineGameStatus(RequestOnlineGameStatus.EMPTY)
        this.player.setNowPlay(PlayersCode.EMPTY.ordinal)
        this.player.setPlayerCode(PlayersCode.EMPTY.ordinal)
        this.player.setIsTechnicalLoss(false)
        this.player.setRemoteMove(RemoteMove())
        return firebaseManager.resetPlayer(this.player)
    }

}