package com.example.chekersgamepro.db.remote

import com.example.chekersgamepro.data.move.RemoteMove
import com.example.chekersgamepro.models.player.data.PlayerData
import com.example.chekersgamepro.models.player.online.IOnlinePlayerEvent
import com.example.chekersgamepro.models.user.IUserProfile
import com.example.chekersgamepro.screens.homepage.RequestOnlineGameStatus
import com.example.chekersgamepro.screens.homepage.menu.online.dialog.DialogStateCreator
import com.example.chekersgamepro.screens.homepage.menu.topplayers.model.ITopPlayer
import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

interface IRemoteDb {

    fun createUser(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<IUserProfile>

    fun createPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Single<Optional<PlayerData>>

    fun createTopPlayer(id: Long, userName: String, encodeImageDefaultPreUpdate: String): Completable

    fun createPlayer(): Single<Optional<PlayerData>>

    fun isUserNameExistServer(userName: String): Single<Boolean>

    fun setIsCanPlay(isCanPlay : Boolean) : Completable?

    fun getDataPlayerChanges() : Observable<PlayerData>

    fun getAllAvailableOnlinePlayersByLevel(): Observable<List<IOnlinePlayerEvent>>

    fun sendRequestOnlineGame(idRemotePlayer : Long) : Completable

    fun declineOnlineGame(): Completable

    fun acceptOnlineGame(): Completable

    fun finishRequestOnlineGame() : Completable

    fun cancelRequestGame(): Completable

    fun notifyEndTurn(move: RemoteMove): Completable

    fun getRemoteMove() : Observable<RemoteMove>

    fun setFinishGameTechnicalLoss(): Completable

    fun isTechnicalWin(): Observable<Boolean>

    fun setMoney(money : Int) : Completable

    fun setTotalGamesUserAndPlayer(totalLoss: Int, totalWin : Int): Completable

    fun resetPlayer() : Completable

    fun setImageProfileAndPlayer(encodeImage: String): Completable

    fun setImageDefaultPreUpdate() : Single<ByteArray?>

    fun getDialogState(): Observable<DialogStateCreator>

    fun startOnlineGame() : Observable<Boolean>

    fun isStillSendRequest(): Observable<Boolean>

    fun getRemotePlayerById(playerId: Long): PlayerData

    fun setDialogCreator(dialogStateCreator: DialogStateCreator)

    fun getRequestGameStatus() : Observable<RequestOnlineGameStatus>

    fun isRelevantRequestGame() : Single<Boolean>

    fun setDeclineRequestGameStatus(): Completable

    fun setTopPlayer(totalWin: Int, totalLoss: Int, moneyByGameResult: Int): Completable
    fun getTopPlayersListByMoney(): Observable<List<ITopPlayer>>

    fun createDialogStateCreator(): Disposable

}